# MSPR - LDAP backend
## Overview
Project developped for learning purpose at EPSI school.
This Springboot API backend intends to run with an angular front-end that you can find at [this page](https://github.com/oulanbator/mspr-ldap-front).

This API manages a 2 factors authentication, checking credentials in a LDAP related server, and managing the 2nd factor (TOTP token) in its own database. At first connection to LDAP, the user is invited to activate the account with a TOTP application (i.e. Google Authenticator). Once the account is activated, the user can log in with his credentials and TOTP code.

## Minimal setup
Assuming you have the Java 11 SDK in your JAVA_HOME and Maven installed, building this application locally is fairly straightforward :

```
git clone https://github.com/oulanbator/mspr-ldap-back
cd mspr-ldap-back/
mvn clean install
cd target/
java -jar ldap-back-0.0.1-SNAPSHOT.jar
```

That's it !