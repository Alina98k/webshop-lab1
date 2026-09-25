<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!--
  =============================================================================
  warehouse/orders.jsp — Depo (WAREHOUSE) için sipariş listeleme/pack ekranı
  =============================================================================

  Amaç
  -----
  • Depo personeli, siparişleri duruma göre (CREATED / PACKED / SHIPPED) görsün.
  • CREATED durumundaki siparişleri "Pack" eylemiyle PACKED durumuna geçirebilsin.

  Ödev İlişkisi (Hangi Kısım?)
  ----------------------------
  • Betyg 5:
    - "Lagerpersonal ska kunna titta på ordrar och 'packa' dem." → Pack butonu.
    - Yetki ayrımı (WAREHOUSE rolü) — bu ekran RoleFilter ile korunmalıdır.
  • Betyg 4:
    - Durum güncellemesi (PACKED) server tarafında transaction içinde yapılır
      (WarehouseController → OrderDaoJdbc.updateStatus(tx,...)).

  MVC Bağlamı
  -----------
  Controller: WarehouseController
    - GET  /warehouse/orders?status=CREATED|PACKED|SHIPPED → listeyi getirir (list, status)
    - POST /warehouse/pack (id) → siparişi PACKED yapar (transaction)
  View: Bu JSP (orders.jsp)
  Model: Order (id, userId, total, createdAt, status)

  Teknik Notlar
  -------------
  • UTF-8 içerik tipi Türkçe uyumu için set edildi.
  • "status" ve "list" request attribute'leri Controller tarafından atanır.
  • CREATED filtreliyken satırlarda "Pack" formu görünür, diğer durumlarda görünmez.
  • "Pack" formu POST /warehouse/pack adresine id parametresi ile gider.
  • ${pageContext.request.contextPath} tabanlı URL üretimi, uygulama kökü değişse bile stabil.
  • Tarih/sayıların formatlanması için JSTL fmt taglib ileride eklenebilir.

  Güvenlik
  --------
  • Bu ekran yalnızca WAREHOUSE rolüne açık olmalıdır (RoleFilter).
  • Pack POST'u CSRF koruması ile zenginleştirilebilir (token).
  • 403/redirect davranışları RoleFilter veya error-page ile özelleştirilebilir.

  Geliştirme Fikirleri
  --------------------
  • Sipariş detay popup/sayfası (kalemler, adres, notlar).
  • Paketleme sonrası SHIPPED geçişi ve kargo takip numarası alanı.
  • Sayfalama/sıralama/filtreleme (tarih aralığı, kullanıcı, tutar).
  • "Pack" işlemi için çoklu seçim ve toplu işlem.
  =============================================================================
-->

<html>
<head>
  <title>Depo - Siparişler</title>
</head>
<body>

<h2>Depo - Siparişler (Durum: ${status})</h2>

<!-- Durum filtreleri -->
<p>
  Görüntüle:
  <a href="${pageContext.request.contextPath}/warehouse/orders?status=CREATED">CREATED</a> |
  <a href="${pageContext.request.contextPath}/warehouse/orders?status=PACKED">PACKED</a> |
  <a href="${pageContext.request.contextPath}/warehouse/orders?status=SHIPPED">SHIPPED</a>
</p>

<!-- Sipariş tablosu -->
<table border="1" cellpadding="6" cellspacing="0">
  <tr>
    <th>ID</th>
    <th>Kullanıcı</th>
    <th>Tutar</th>
    <th>Oluşturulma</th>
    <th>Durum</th>
    <th>İşlem</th>
  </tr>

  <c:forEach var="o" items="${list}">
    <tr>
      <td>${o.id}</td>
      <td>${o.userId}</td>
      <td>${o.total}</td>
      <td>${o.createdAt}</td>
      <td>${o.status}</td>
      <td>
        <!-- Yalnızca CREATED ise Pack eylemi gösterilir -->
       <c:if test="${status == 'CREATED'}">
         <form method="post" action="${pageContext.request.contextPath}/warehouse/pack" style="display:inline">
           <input type="hidden" name="id" value="${o.id}">
           <button type="submit">Pack</button>
         </form>
       </c:if>

       <c:if test="${status == 'PACKED'}">
         <form method="post" action="${pageContext.request.contextPath}/warehouse/ship" style="display:inline">
           <input type="hidden" name="id" value="${o.id}">
           <button type="submit">Ship</button>
         </form>
       </c:if>

      </td>
    </tr>
  </c:forEach>
</table>

<p><a href="${pageContext.request.contextPath}/home">Anasayfa</a></p>
</body>
</html>
