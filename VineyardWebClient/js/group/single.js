var loadWorkGroup = function () {
    // get id from url
	var id = $("#group-id").val();
    var requestedUrl = vineyard.config.serverUrl + "group/" + id;

    $.getJSON(requestedUrl, function (group) {

        // Name
        $("#group-name").val(group.name);

        // Description
        $("#group-description").val(group.description);

        // Workers
        var workers = "";
        $.each(group.workers, function(index, worker) {
            // TODO get name workers?
            workers += worker + ", ";
        });

        if (workers.length > 0)
            workers = workers.substring(0, workers.length - 2);

        $("#group-workers").val(workers);
    });
}

loadWorkGroup();