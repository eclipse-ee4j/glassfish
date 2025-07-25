package org.hibernate.data.tck.ext;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import org.hibernate.engine.jndi.JndiException;
import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;

/**
 * Provides the JTA platform for GlassFish.
 */
@ApplicationScoped
public class GlassFishJtaPlatform extends AbstractJtaPlatform {
    public static final String UT_NAME = "java:comp/UserTransaction";
    public static final String TM_NAME = "java:appserver/TransactionManager";
    private TransactionManager transactionManager;

    @Override
    protected boolean canCacheUserTransactionByDefault() {
        return true;
    }

    @Override
    protected boolean canCacheTransactionManagerByDefault() {
        return true;
    }

    @Override
    protected TransactionManager locateTransactionManager() {
        if(transactionManager == null) {
            try {
                transactionManager = (TransactionManager) jndiService().locate( TM_NAME );
            }
            catch (JndiException jndiException) {
                throw new JndiException( "unable to find TransactionManager", jndiException );
            }
        }
        return transactionManager;
    }

    @Override
    protected UserTransaction locateUserTransaction() {
        try {
            return (UserTransaction) jndiService().locate( UT_NAME );
        }
        catch (JndiException jndiException) {
                throw new JndiException( "unable to find UserTransaction", jndiException );
        }
    }
}
