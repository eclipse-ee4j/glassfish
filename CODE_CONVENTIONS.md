Eclipse/Sun code conventions with
* Spaces only
* Indentation size 4 spaces
* Maximum line width 160
* Maximum width for comments 120
* No indent of Javadoc tags
* No newline after @param tags

See also: https://github.com/eclipse-ee4j/ee4j/tree/master/codestyle

## Javadoc

### Parameters

```
@param a The first parameter. For an optimum result, this should be an odd
number between 0 and 100.
```

## Variable naming style

Based on the advice from Uncle Bob's Clean Code, specifically:

* No cryptic abbreviations like c, ta, rx, ct, except for the well-established i and j in loops
* No variable names like ret, rvalue, result etc for variables that are returned from methods. Instead, they should be named after what they actually return.

For example:

Bad:

```java
public Permissions getCallerPermission(....) {
    Permissions rvalue;
    // ton of code

    return rvalue;
}
```

Good:

```java
public Permissions getCallerPermissions(....) {
    Permissions callerPermissions;
    // ton of code

    return callerPermissions;
}
```

* No Hungarian variations for collections like usrLst, usArray, arrUsers, UserCol, etc, and no such variation for elements of the collection like el, elm, usrEl, userElem, currentUsr, curUser, userCr, etc. Omit the Hungarian and use the element type name directly and the plural of that for the collection.  

For example:

Bad:

```java
for (User curUsr : colUser) {
     ...
}
```

Good:

```java
for (User user : users) {
     ...
}
```

## Conditional blocks

### Handle the short and fast error case for method parameters early instead of the happy path. 

For example:

Bad:

```java
public void foo(Bar bar) {
    if (bar != null) {
        // lots of code here
    } else {
        throw new IllegalStateException("Bar should not be null");
    }
}
```

Good:

```java
public void foo(Bar bar) {
    if (bar == null) {
        throw new IllegalStateException("Bar should not be null");
    }

    // lots of code here
}
```

### if/else blocks that return don't need to be if/else blocks. 

For example:

Bad:

```java
if (foo == something) {
   return somethingFoo;
} else if (foo == somethingElse) {
   return somethingElseFoo;
}
```
   
Good:

```java
if (foo == something) {
   return somethingFoo;
}

if (foo == somethingElse) {
   return somethingElseFoo;
}
```


## Defaults

### Omit initialisation of instance variables to their default values. 

For example:

Bad:

```java
public class SomeClass {
    private int someNumber = 0;
    private Foo someFoo = null;
    private boolean isFoo = false;
}
```

Good:

```java
public class SomeClass {
    private int someNumber;
    private Foo someFoo;
    private boolean isFoo;
}
```

### Omit using the public modifier for interface methods.

For example:

Bad:

```java
public interface MyInterface {
    public void MyMethod();
}
```

Good:

```java
public interface MyInterface {
    void MyMethod();
}
```

### Omit unnecessary usage of `this`

Bad:

```java
public Foo getFoo() {
    return this.foo;
}
```

Good:

```java
public Foo getFoo() {
    return foo;
}
```

### Omit unnecessary braces. 

For example:

Bad

```java
return (1);
```

Good

```java
return 1;
```