<%--
  Created by IntelliJ IDEA.
  User: 
  Date: 2019/4/7
  Time: 14:45
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>EGO注册</title>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.11.1.min.js"></script>
</head>
<body>
<form action="${pageContext.request.contextPath}/doResigter" method="post">
    用户名 <input type="text" id="username" name="username" placeholder="请输入用户名"> <br>
    密码&nbsp; &nbsp; <input type="password" name="pwd" placeholder="请输入密码"> <br>
    验证码 <input type="text" name="code" placeholder="请输入验证码">
    <br>
    <input type="submit" value="注册">
</form>
<input type="button" name="btn" id="btn" value="点击获取验证码"></input>

</body>
<script>
    $("#btn").on("click", function () {
        $.post("${pageContext.request.contextPath }/code/getCode", {"username": $("#username").val()}, function (res) {
            if (res.status == 200) {
                alert(res.data);
            }
        });
    });

</script>
</html>
