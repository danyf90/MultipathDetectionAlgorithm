var loadWorker = function () {
    // get id from url
	var id = $("#worker-id").attr("value");
    var requestedUrl = vineyard.config.serverUrl + "worker/" + id;
	
    $.getJSON(requestedUrl, function (worker) {
        
        // Name
        $("#worker-name").attr("value", worker.name);
        
        // Description
        $("#worker-username").attr("value", worker.username);
        
        // Email
        $("#worker-email").attr("value", worker.email);
        
        // Roles
        $("#worker-roles").attr("value", worker.roles);
        
        $("#loading").css("visibility", "hidden");
    });
}

loadWorker();