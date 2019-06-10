<%--
  Created by IntelliJ IDEA.
  User: 
  Date: 2019/4/10
  Time: 17:11
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>订单检查回显</title>
    <script src="${pageContext.request.contextPath }/js/jquery-1.5.1.min.js"></script>
</head>
<body>
<div id="orderList"></div>
</body>
<script>
    $(function () {
        //查询当前用户的所有订单
        $.post("${pageContext.request.contextPath }/order/query", {"status": 0}, function (res) {
            if (res.status == 200) {
                var orderInfo = res.data.data;
                var html = "";
                $.each(orderInfo, function (index, item) {
                    html += "<ul><li><div><div ><lable>订单名称：</lable><input id='" + index + "' value='" + item.orderSn + "' readonly='true'/></div><div><lable>价格：</lable>" + item.totalAmount + "</div><div><button onclick='pay(" + index + ")'>立即支付</button>  </div></div></li></ul>";
                });
                $("#orderList").html(html);
            } else {
                alert(res.msg);
            }
        });


    });

    function pay(index) {
        //跳转到支付系统的支付里面
        var orderSn = "#" + index;
        var sn = $(orderSn).val();
        location.href = "http://pay.ego.com:8083/toPay?orderSn=" + sn;
    }
</script>
</html>
