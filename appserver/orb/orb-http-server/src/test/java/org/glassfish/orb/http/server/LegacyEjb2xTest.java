package org.glassfish.orb.http.server;



import jakarta.ejb.CreateException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBObject;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.glassfish.orb.http.client.HttpInitialContextFactory;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.RemoteEjbReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The EJB 2.x client shape, over a real connection.
 *
 * <p>An application written against IIOP in 2006 looks like this, and is the
 * kind with the strongest reason to move off IIOP and the least appetite for
 * being rewritten first. If the migration story only covers the EJB 3 business
 * view then it does not cover the applications it was written for.
 */
class LegacyEjb2xTest {

    private static final String JNDI_NAME = "java:global/myapp/CartBean";

    // ---- the 2.x shape: two interfaces, not one ----------------------------

    public interface Cart extends EJBObject {

        void addItem(String item) throws RemoteException;

        List<String> items() throws RemoteException;
    }

    public interface CartHome extends EJBHome {

        Cart create() throws RemoteException, CreateException;
    }

    /** The bean. The EJBObject operations are the container's, never its own. */
    public static final class CartBean implements Cart {

        private final List<String> items = new ArrayList<>();

        @Override
        public void addItem(String item) {
            items.add(item);
        }

        @Override
        public List<String> items() {
            return new ArrayList<>(items);
        }

        @Override
        public jakarta.ejb.EJBHome getEJBHome() {
            throw new UnsupportedOperationException("handled by the client proxy");
        }

        @Override
        public Object getPrimaryKey() {
            throw new UnsupportedOperationException("handled by the client proxy");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("handled by the container");
        }

        @Override
        public jakarta.ejb.Handle getHandle() {
            throw new UnsupportedOperationException("handled by the client proxy");
        }

        @Override
        public boolean isIdentical(EJBObject other) {
            throw new UnsupportedOperationException("handled by the client proxy");
        }
    }

    private RealHttpServer server;

    @BeforeEach
    void setUp() throws Exception {
        FakeContainer container = new FakeContainer();
        container.registerStateful("CartBean", CartBean::new);

        HttpRoundTripTest.FakeNaming naming = new HttpRoundTripTest.FakeNaming();
        naming.bindings.put(JNDI_NAME, RemoteEjbReference.home(
                "myapp", "mymodule", null, "CartBean",
                CartHome.class.getName(), Cart.class.getName()));

        EjbDispatcher ejb = new EjbDispatcher(container, SecurityBridge.NONE,
                TransactionBridge.NONE, new JavaSerializationMarshaller(),
                new InvocationRegistry(), new SessionAffinity("instance-1"));
        server = new RealHttpServer(ejb, new NamingDispatcher(naming),
                new AffinityDispatcher(new SessionAffinity("instance-1")));
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.close();
        }
    }

    private InitialContext context() throws Exception {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, HttpInitialContextFactory.class.getName());
        env.put(Context.PROVIDER_URL, server.baseUri().toString());
        Hashtable<String, String> table = new Hashtable<>();
        env.forEach((k, v) -> table.put(String.valueOf(k), String.valueOf(v)));
        return new InitialContext(table);
    }

    // ---- the flow ----------------------------------------------------------

    @Test
    @DisplayName("lookup, narrow, create, invoke, remove - the whole 2.x conversation")
    void theCompleteLegacyFlow() throws Exception {
        InitialContext ctx = context();
        try {
            Object looked = ctx.lookup(JNDI_NAME);
            CartHome home = (CartHome) looked;

            Cart cart = home.create();
            assertNotNull(cart);

            cart.addItem("book");
            cart.addItem("pen");
            assertEquals(List.of("book", "pen"), cart.items());

            cart.remove();

            // After remove the reference is dead, not merely idle.
            assertThrows(Exception.class, () -> cart.items());
        } finally {
            ctx.close();
        }
    }

    @Test
    @DisplayName("PortableRemoteObject.narrow would return the looked-up object unchanged")
    void theLookedUpObjectSatisfiesNarrow() throws Exception {
        InitialContext ctx = context();
        try {
            Object looked = ctx.lookup(JNDI_NAME);

            // Every narrow implementation begins by checking exactly this, and
            // returns its argument when it holds. So a legacy client's
            // PortableRemoteObject.narrow(o, CartHome.class) is a no-op here -
            // which is why the migration is to delete the call, not rewrite it.
            assertTrue(CartHome.class.isAssignableFrom(looked.getClass()),
                    "narrow returns its argument unchanged only if this holds");
            assertSame(looked, CartHome.class.cast(looked));
        } finally {
            ctx.close();
        }
    }

    @Test
    @DisplayName("two create() calls give two independent conversations")
    void eachCreateIsItsOwnSession() throws Exception {
        InitialContext ctx = context();
        try {
            CartHome home = (CartHome) ctx.lookup(JNDI_NAME);

            Cart first = home.create();
            Cart second = home.create();

            first.addItem("only mine");

            assertEquals(List.of("only mine"), first.items());
            assertEquals(List.of(), second.items());
            assertFalse(first.isIdentical(second));
            assertTrue(first.isIdentical(first));
        } finally {
            ctx.close();
        }
    }

    @Test
    void theComponentReferenceKnowsItsHome() throws Exception {
        InitialContext ctx = context();
        try {
            CartHome home = (CartHome) ctx.lookup(JNDI_NAME);
            Cart cart = home.create();

            assertNotNull(cart.getEJBHome(), "getEJBHome must answer without a round trip");
            assertTrue(cart.getEJBHome() instanceof CartHome);
        } finally {
            ctx.close();
        }
    }

    @Test
    @DisplayName("what is not supported fails loudly rather than silently")
    void unsupportedLegacyOperationsAreRefused() throws Exception {
        InitialContext ctx = context();
        try {
            CartHome home = (CartHome) ctx.lookup(JNDI_NAME);
            Cart cart = home.create();

            // A session bean has no primary key, and handles carry machinery
            // this transport does not. Answering null would be worse.
            assertThrows(RemoteException.class, cart::getPrimaryKey);
            assertThrows(RemoteException.class, cart::getHandle);
            assertThrows(Exception.class, home::getEJBMetaData);
            cart.remove();
        } finally {
            ctx.close();
        }
    }
}
