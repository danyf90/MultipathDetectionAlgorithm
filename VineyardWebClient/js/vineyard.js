var vineyard = {
    config: {
        serverUrl: "http://vineyard-server.no-ip.org/api/"
    }
};

$(function() {
	var path = window.location.pathname.replace(/\/$/, '').replace(/^\//, '');

	var resource = path.split("/")[0];

	if (resource.length > 0)
		$(".menu-item-" + resource).addClass("current");

	$loading = $("#loading");

	$(document).ajaxStart(function() {
		$loading.css("visibility", "visible");
	}).ajaxStop(function(){
		$loading.css("visibility", "hidden");
	});

	// TODO you can do better than this!
	var requestedUrl = vineyard.config.serverUrl + "issue/open/" ;
	$.getJSON(requestedUrl, function (issues) {
		$("#open-issues").text(issues.length);
	});

	$("#logged-worker-name").text(sessionStorage.getItem("workerName"));
});