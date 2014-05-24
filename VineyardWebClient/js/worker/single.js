var loadWorker = function () {
    // get id from url
	var id = $("#worker-id").val();
    var requestedUrl = vineyard.config.serverUrl + "worker/" + id;
    $.getJSON(requestedUrl, function (worker) {
        // Name
        $("#worker-name").val(worker.name);
        
        // Description
        $("#worker-username").val(worker.username);
        
        // Email
        $("#worker-email").val(worker.email);
        
        // Roles
        $("#worker-roles").val(worker.roles);
    });
}

loadWorker();