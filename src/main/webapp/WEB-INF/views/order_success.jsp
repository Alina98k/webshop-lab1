<%@ page contentType="text/html; charset=UTF-8" %>
<!--
============================================================================
order_success.jsp — Orderbekräftelsesida (View-lagret)
============================================================================

Syfte
-----
• Visa ordernumret för användaren efter att beställningen har genomförts
och transaktionen har lyckats.
• Ge en länk tillbaka till startsidan.

Kurskoppling
------------
• Betyg 4:
- “Ordrar ska vara inom en transaktion.”
När OrderService har genomfört ordern (huvud + rader + lagerjustering)
i en och samma transaction, skickar Controller (OrderController)
vidare hit.
• Betyg 3:
- Avslutar flödet “Lägga saker i korgen och titta i den”:
varukorg → skapa order → visa bekräftelse.

MVC-sammanhang
--------------
• Controller: OrderController#handlePlace (POST /orders/place)
- Vid lyckad order → request.setAttribute("orderId", id) + forward till denna sida
- Vid fel → tillbaka till cart.jsp och visa felmeddelande
• Service: OrderService.placeOrder (transaction, commit/rollback)
• View: Denna JSP (order_success.jsp)

Tekniska detaljer
-----------------
• UTF-8 säkerställer korrekt teckenkodning.
• ${orderId} kommer från request-attributet.
• Navigationslänkar använder contextPath som bas.
• JSTL fmt-taggen kan läggas till för valuta/datum vid behov.

Förbättringsidéer
-----------------
• Länk till “Orderdetaljer” (artiklar, adress, leveransstatus).
• Länk till “Mina ordrar”.
• Skicka e-postbekräftelse (i service-lagret).
============================================================================
-->

<html>
<head><title>Beställning mottagen</title></head>
<body>s
<h2>Din beställning har genomförts!</h2>
<p>Ditt ordernummer är: <strong>${orderId}</strong></p>

<p><a href="${pageContext.request.contextPath}/home">Till startsidan</a></p>
</body>
</html>
