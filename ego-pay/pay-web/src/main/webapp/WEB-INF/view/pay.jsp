<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>EGO|PAY</title>

    <script src="/js/jquery.min.js"></script>
    <script src="/js/qrcode.js"></script>
</head>
<body>
<div id="qrcode"></div>

<form action="" id="payFrm" method="post">
    支付类型<input type="text" id="payType" name="payType" placeholder=""/><br/>
    订单号<input type="text" id="orderSn" name="outTradeNo" placeholder="" value="${order.outTradeNo}"/><br/>
    订单名称<input type="text" name="subject" placeholder="" value="${order.orderName}"/><br/>
    总金额<input type="text" name="totalAmount" placeholder="" value="${order.totalAmount}"/><br/>
    商品描述<input type="text" name="body" placeholder="" value="${order.orderInfo}"/><br/>
    商店<input type="text" name="storeId" placeholder=""/><br/>
    超时时间<input type="text" name="timeoutExpress" placeholder=""/><br/>
    商品码<input type="text" name="productCode" placeholder=""/><br/>
</form>
<input type="button" id="btn" value="支付">
</body>
<script>

    $(function () {
        $("#btn").on("click", function () {
            $.post("/doPay", $("#payFrm").serialize(), function (res) {
                alert(res.data);
                if (res.status == 200) {
                    //成功了 在判断是扫码还是电脑支付
                    if ($("#payType").val() == 1) {
                        //是扫码支付 选中div 清空 在赋值
                        $("#qrcode").html("");
                        $("#qrcode").qrcode(res.data);
                        //一个定时任务 去观察数据库订单的改变
                        setInterval(function () {
                            $.post("http://pay.ego.com:8083/order/query", {"orderSn": $("#orderSn").val()}, function (res) {
                                if (res.status == 200) {
                                    if (res.data.payStatus == 2) {
                                        location.href = "http://pay.ego.com:8083/pay/ok?orderSn=" + $("#orderSn").val();
                                    }
                                }
                            });
                        }, 2000);
                    }
                    if ($("#payType").val() == 2) {
                        //是电脑支付
                        $("#qrcode").html("");
                        $("#qrcode").html(res.data);
                        return;
                    }
                    alert(res.msg);
                }
            });
        })
    });


</script>

</html>