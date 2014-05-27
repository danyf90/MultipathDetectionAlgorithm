S = {};

var init = function () {

	sessionStorage.removeItem("workerId");

	S.form = $("form");
	S.form.on("submit", function () {

		var serverUrl = $("#server").val() + "api/worker/login";
		$("#password").val(CryptoJS.MD5($("#password").val()));

		try {
			$.post(serverUrl, S.form.serialize(), function (data) {
				data = $.parseJSON(data);
				sessionStorage.setItem("workerId", data.id);
				sessionStorage.setItem("workerName", data.name);
				$(".ajax").removeAttr("name");
				$("#login-id").attr("name", "login");
				S.form.off("submit").submit();
			});

		} catch (e) { alert("login error"); }

		return false;
	});
};

init();