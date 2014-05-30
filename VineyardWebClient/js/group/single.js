// some shared state
var S = {};

///////////////////////////////
/// GROUP INSERTION
///////////////////////////////

var insertGroup = function () {
	// TODO minimum validation!
	var requestedUrl = vineyard.config.serverUrl + "group/";
	$.post(requestedUrl, $("form").serialize(), function (data, xhr) {
		
		data = $.parseJSON(data);
		
		if (xhr.status != 201) {
			console.log(data);
			return;
		}
		
		window.location = "/group/" + data.id;
	});
};

var loadGroupInsertion = function () {
	$("#group-id").remove();
	$("#group-add-worker").remove();
	
	$("#control-ok").on("click", insertGroup);
	$("#control-cancel").on("click", function(){ window.location = "/group"; });
	$("#group-workers").append('<tr><th></th><td style="font-size: 0.7em;">NOTE: Workers can be added once a group has been created.</td></tr>');
	
	$("table input, table select").on("change", function () { this.name = this.dataset.name; });
};

///////////////////////////////
/// GROUP MODIFICATION
///////////////////////////////

var showError = function () {
    // TODO
    console.log("Something gone wrong..");
	console.log(arguments);
};
			
var resetForm = function () {
	S.controls.css("visibility", "hidden");
	S.groupAddWorker.css("visibility", "visible");
	$("input, select").not(".worker").removeAttr("name");
	$("input.worker").removeClass("changed");
	$(".new-worker-key").each(function(){
		var $this = $(this);
		
		var key = this.value;
		var value = $this.parents("tr").find(".new-worker").val();
		
		addWorkerRow(key, value);
		$this.parents("tr").remove();
	});
};

var addWorkerRow = function (key, value) {
	var $row = $('<tr><th>Worker #' + (parseInt(key) + 1) + '</th><td style="display: flex;">' + S.workers[value].name + '</td></tr>');
	
	var $deleteSpan = $('<span class="delete-row" data-id="' + value + '"></span>');
	$deleteSpan.on("click", removeWorker);
	$row.find("td").append($deleteSpan);
	
	S.groupWorkers.append($row);
};

var addNewWorker = function () {
	
	var $select = $('<select class="new-worker"></select>');
	
	for (var i in S.workers)
		if (S.group.workers == null || S.group.workers.indexOf(i) == -1)
			$select.append('<option value="' + S.workers[i].id + '">' + S.workers[i].name + '</option>');

	var $row = $('<tr><th></th><td style="display: flex;"></td></tr>');
	var $deleteSpan = $('<span class="delete-row"></span>');
	$deleteSpan.on("click", removeWorker);
	$row.find("td").append($select).append($deleteSpan);
	
	S.groupWorkers.append($row);
	S.controls.css("visibility","visible");	
};

var removeWorker = function () {
	// this = clicked span
	$span = $(this);
	var removeId = $span.data("id");
	console.log(removeId);
	if (removeId != null) { // remove alredy present worker
		var requestOptions = {
			url: vineyard.config.serverUrl + "group/" + S.group.id + "/worker/" + removeId,
			type: 'DELETE'
		};
		
		$.ajax( requestOptions ).done(function () {
			$span.parents("tr").remove();
		});
	} else { // remove uncommitted worker
		$span.parents("tr").remove();
	}
};

var loadWorkers = function () {
	var requestedUrl = vineyard.config.serverUrl + "worker/";
	return $.getJSON(requestedUrl, function(workers) {
		S.workers = {};
		$.each(workers, function (index, worker) { S.workers[worker.id] = worker; });
	});
};

var loadGroup = function(id) {
	
	var workerRequest = loadWorkers();
	
    var requestedUrl = vineyard.config.serverUrl + "group/" + id;
    return $.getJSON(requestedUrl, function (group) {
		S.group = group;
        // Name
        $("#group-name").val(group.name); 
        // Description
        $("#group-description").val(group.description);     
		
		// Workers
		$.when( workerRequest ).done(function () {
			if (group.workers !== undefined)
				for (var i in group.workers)
					addWorkerRow(i, group.workers[i]);  

			S.groupAddWorker.on("click", addNewWorker);
		});
    });
};

var commitGroupChanges = function () {
	
	var data = $("form").serialize();
	
	var saveWorkers = function () {
		var defObjs = [];
		
		$(".new-worker").each(function(){	
			var requestOptions = {
				url: vineyard.config.serverUrl + "group/" + S.group.id + "/worker/" + this.value,
				type: 'PUT'
			};
			
			defObjs.push( $.ajax(requestOptions) );
		});
		
		$.when(defObjs).done(function () { window.location.reload(); });
	};
	
	if (data != "")
		$.ajax({
			type: 'PUT',
			url: vineyard.config.serverUrl + "group/" + S.group.id,
			data: data
		})
		.done(saveWorkers)
		.fail(showError);
	else saveWorkers();
};

var loadGroupModification = function (id) {
	
	var groupRequest = loadGroup(id);
	
	S.controls.css("visibility", "hidden");
	
	$.when(groupRequest).then(function () {
				
		$("#control-ok").on("click", commitGroupChanges);
		
		$("input, select").on("change", function () {
			if (!$(this).hasClass("worker"))
				this.name = this.dataset.name;
			S.controls.css("visibility", "visible");
			S.groupAddWorker.css("visibility", "hidden");
		});
		
		$("input.worker").on("change", function() {
			$(this).addClass("changed");
		});
		
	});
};

var init = function() {
	// get id from url
	var id = $("#group-id").val();	
	
	S.controls = $(".controls");
	S.groupAddWorker = $("#group-add-worker");		
	S.groupWorkers = $("#group-workers");
	
	$("#control-cancel").on("click", function () { window.location = "/group/" + id; });

	if (id == "insert")
		loadGroupInsertion();
	else loadGroupModification(id);
};

init();