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
});