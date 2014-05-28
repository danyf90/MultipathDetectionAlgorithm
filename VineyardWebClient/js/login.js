S = {};

var init = function () {

	sessionStorage.removeItem("workerId");

	S.form = $("form");
	S.form.on("submit", function () {
		$("#loading").css("visibility", "visible");
		var serverUrl = $("#server").val() + "api/worker/login";
		
		var password = $("#password").val();
		$("#password").val(CryptoJS.MD5(password));

		// if it contains '@' it is an email
		if ($("#username").val().indexOf("@") >= 0)
			$("#username").attr("name", "email");
		
		
		$.ajax({
			type: "POST",
			url: serverUrl,
			data: S.form.serialize(),
			success: function (data) {
				$("#password").val(password);
				data = $.parseJSON(data);
				sessionStorage.setItem("workerId", data.id);
				sessionStorage.setItem("workerName", data.name);
				$(".ajax").removeAttr("name");
				$("#login-id").attr("name", "login");
				S.form.off("submit").submit();
				$("#loading").css("visibility", "hidden");
			},
			error: function(XMLHttpRequest, textStatus, errorThrown) {
				$("#password").val(password);
				$("#loading").css("visibility", "hidden");
				alert("login error");
			}
		});

		return false;
	});
};

init();