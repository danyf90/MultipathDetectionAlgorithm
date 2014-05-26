S = {};

var showError = function () {
    // TODO
    console.log("Something gone wrong..");
	console.log(arguments);
};

var insertWorker = function () {
		// TODO minimum validation!
	var requestedUrl = vineyard.config.serverUrl + "worker/";
	$.post(requestedUrl, $("form").serialize(), function (data, e, xhr) {
		data = $.parseJSON(data);
		
		if (xhr.status != 201) {
			console.log(data);
			return;
		}
		
		window.location = "/worker/" + data.id;
	});
};

var loadWorkerInsertion = function () {
	$("input, select").not("#worker-role").on("change", function () {
		this.name = this.dataset.name;
	});

	$("#worker-role").on("change", function () {
		var values = [];
		for (var i = 0, l = this.selectedOptions.length; i < l; i++)
			values.push(this.selectedOptions[i].value);

		this.nextSibling.value = values.join(",");
	});
	
	$("#control-ok").on("click", insertWorker);
};

var loadWorker = function (id) {
    
    var requestedUrl = vineyard.config.serverUrl + "worker/" + id;
    $.getJSON(requestedUrl, function (worker) {
		S.worker = worker;
        // Name
        $("#worker-name").val(worker.name);
        // Description
        $("#worker-username").val(worker.username);
        // Email
        $("#worker-email").val(worker.email);
        // Roles
		var roles = worker.role.split(",");
		for(var i in roles)
			$("#worker-role option[value='" + roles[i] + "']").attr("selected", "selected");
    });
};

var commitWorkerChanges = function () {
	
	var data = $("form").serialize();
	
	if (data != null)
		$.ajax({
			type: 'PUT',
			url: vineyard.config.serverUrl + "worker/" + S.worker.id,
			data: data,
			success: function() { window.location.reload(); }
		}).fail(showError);
};

var loadWorkerModification = function (id) {
	
	var workerRequest = loadWorker(id);
	
	S.controls.css("visibility", "hidden");
	
	$.when(workerRequest).then(function () {
		
		$("#control-ok").on("click", commitWorkerChanges);
		
		$("input, select").not("#worker-role").on("change", function () {
			this.name = this.dataset.name;
			S.controls.css("visibility", "visible");
		});
		
		$("#worker-role").on("change", function () {
			var values = [];
			for (var i = 0, l = this.selectedOptions.length; i < l; i++)
				values.push(this.selectedOptions[i].value);
			
			this.nextElementSibling.value = values.join(",");
			this.nextElementSibling.name = this.nextElementSibling.dataset.name;
			
			S.controls.css("visibility", "visible");
		});
	});
};


var init = function() {
	// get id from url
	var id = $("#worker-id").val();	
	
	S.controls = $(".controls");
	$("#control-cancel").on("click", function () { window.location.reload(); });
	
	if (id == "insert")
		loadWorkerInsertion();
	else loadWorkerModification(id);
};

init();