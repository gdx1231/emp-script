===========================
Setup DKIM for JavaMail API
===========================

What is required to use DKIM for JavaMail
---

- a project using JavaMail: http://java.sun.com/products/javamail/
  (DKIM for JavaMail was tested with version JavaMail 1.4.1 but should be compatible with
   older versions too, see TODO below)


- a DKIM key setup:

  There are test keys in the keys/ directory but be aware to use those only for your tests.
  
  You need:

  a) a private key on your hard disc (e.g. in keys/); you can generate a new key by

   > openssl genrsa -out private.key.pem
 
     DKIM for JavaMail needs the private key in DER format, you can transform a PEM key
     with openssl:

   > openssl pkcs8 -topk8 -nocrypt -in private.key.pem -out private.key.der -outform der


  b) a public key in your DNS; here is a sample ressource record with selector "default":
     default._domainkey IN TXT "v=DKIM1; g=*; k=rsa; p=MIG...the_public_key_here...AQAB"
     (see http://www.ietf.org/rfc/rfc4871.txt for details)
     
     You can use openssl to get a public key from the private key:
   
   > openssl rsa -inform PEM -in private.key.pem -pubout



Run Tests
---

- if you want to run the examples provided in DKIM for JavaMail you have to

  a) configure the config file test.properties (see comments inside)
  
  b) run `ant runTests`


Implement DKIM for JavaMail into your code
---

- add DKIMforJavaMail.jar to your classpath

- see the example files in test/de/agitos/dkim/* : it's very simple to implement, enjoy!


====
TODO
====

- DKIM for JavaMail is in principal compatible with all libraries using JavaMail,
  an example for Apache Commons Email should be added to this project

- test of DKIM for JavaMail with older JavaMail versions, maybe a JAR for Java 1.4;
  I will do this if needed upon request

- verification functionality could be added; this has minor priority at time, I will
  do this if needed upon request (there will be other libraries doing this maybe)

- the optional public key check before sending should be fully implemented; this is
  out of the DKIM basic functionality and therefore isn't mandatory


===============
RELEASE-HISTORY
===============

1.0, 2008-11-30
1.1, 2009-04-13
   Bugfix line-encoding before body canonicalization on *nix systems
   Added partial line-folding of the DKIM-Signature header field
1.2, 2009-05-10
   Bugfix in signing mime messages that are loaded by an InputStream,
   Added a MimeMessageTest for testing the signing of loaded mime messages.
1.3, 2009-07-29
   Changed the insertion of the DKIM-Signature header to the top most position
   when sending mail. This is compatible with the Yahoo! DKIM verifier.


==================
Further DKIM Links
==================

http://www.dkim-reputation.org : DKIM Reputation Project
   Reputation data based on DKIM identities (that is signingdomains and users 'below');
   this project was built especially to setup good reputation for good users to reduce
   the false-positive-problem. The DKIM Reputation Project collects identities of spammers
   as well to prevent that spammers gain positive reputation.
   In addition, this project helps ISPs to identify spamming user accounts by the
   subscription to ARFs (abuse reports).
   Furthermore you can subscribe to daily DKIM checks to assure that the DKIM signatures
   you inject in your mails on your MTA are valid.

http://dkim-connector.agitos.de : DKIM Connector
   With the DKIM Connector you get an configuration tool to update DKIM configs (especially
   public/private keys) on one or more mail servers and in your master DNS. This simplifies
   the setup of DKIM for multiple domains (in virtual hosting environments) and makes it
   possible to automate regular key rotations.
   Considering the DKIM Reputation aspects it is reasonable to sign your mail as precise
   as possible, that means: if you are isp.com you could sign mails from customers with
   domains cust1.com and cust2.com with your signing domain isp.com. This is the simple
   setup. In a more detailed setup you sign mails from cust1.com with signing domain cust1.com
   and cust2.com with signing domain cust2.com

http://www.dkim.org/deploy/ : DKIM implementations
	On this website you can find most MTA-DKIM implementations and complementary products
	and services.

http://java.sun.com/products/javamail/ : Sun JavaMail API

---

http://www.agitos.de/dkim-for-java-mail-open-source-library-2.html
Florian Sager, sager@agitos.de, 22.11.2008
