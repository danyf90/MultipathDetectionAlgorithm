// some shared state
var S = {};

///////////////////////////////
/// TASK INSERTION
///////////////////////////////

var loadTaskInsertion = function () {
	var requestedUrl = vineyard.config.serverUrl + "task/";
	$("#task-id").remove();
	
	/*$.post(requestedUrl, {name: "Luogo nuovo"}, function (data, xhr) {
		if (xhr.status != 201) {
			console.log("Something went wrong..");
			return;
		}
		
		data = $.parseJSON(data);
		$("#task-id").attr('value', data.id);
		$("#task-name").attr('value', "Luogo nuovo");
		
	});*/
};

///////////////////////////////
/// TASK MODIFICATION
///////////////////////////////

var showMap = function () {
    // TODO
    console.log("showMap NOT IMPLEMENTED");
};

var showError = function () {
    // TODO
    console.log("Something gone wrong..");
	console.log(arguments);
};

var resetForm = function () {
	S.controls.css("visibility", "hidden");
};

var loadTask = function(id) {
	
    var requestedUrl = vineyard.config.serverUrl + "worker/";
	var workersRequest = $.getJSON(requestedUrl, function(workers) {
		S.workers = {};
		 $.each(workers, function (index, worker) {
        	S.workers[worker.id] = worker;
			var option = '<option value="' + worker.id + '">' + worker.name + '</option>';
			$("#task-assign-worker").append(option);
        });
	});
	
	requestedUrl = vineyard.config.serverUrl + "group/";
	var groupsRequest = $.getJSON(requestedUrl, function(groups) {
		S.groups = {};
		 $.each(groups, function (index, group) {
        	S.groups[group.id] = group;
			var option = '<option value="' + group.id + '">' + group.name + '</option>';
			$("#task-assign-group").append(option);
        });
	});
	
	requestedUrl = vineyard.config.serverUrl + "place/";
	var placesRequest = $.getJSON(requestedUrl, function (places) {
		S.places = {};
        $.each(places, function (index, place) {
        	S.places[place.id] = place;
			var option = '<option value="' + place.id + '">' + place.name + '</option>';
			$("#task-place").append(option);
        });
	});
	
	// TODO include rev=?
	requestedUrl = vineyard.config.serverUrl + "task/" + id;
    return $.getJSON(requestedUrl, function (task) {
		
		S.task = task;
		
        // Name
        $("#task-title").val(task.title); 
        // Description
        $("#task-description").val(task.description);     
        // Position Link
        if (task.latitude !== undefined)
            $("#task-location-link a").text("Mostra mappa").on("click", showMap);
		// Last changed
		$.when( workersRequest ).done(function(){
			$("#change-date").children().eq(1).text(task.start_time);
			$("#modifier").text(S.workers[task.modifier].name);
		});
		
		// Status
		$("#task-status").addClass(task.status).text(task.status);
		
		// Priority
		$("#task-priority option[value='" + task.priority + "']")[0].selected = "selected";
		
		// Place
		$.when( placesRequest ).done(function(){
			$("#change-date").children().eq(1).text(task.start_time);
			$("#modifier").text(S.workers[task.modifier].name);
		});
		
		// Assign time
		if (task.assign_time != null)
			$("#task-assign-th").append('<span class="assign-time">' + task.assign_time + '</span>');
		
		// Assigned worker
		if (task.assign_worker != null)
			$("#task-assign-worker option[value='" + task.assign_worker + "']").selected = "selected";
		// Assigned group
		else if (task.assign_group != null)
			$("#task-assign-group option[value='" + task.assign_group + "']").selected = "selected";
		
		// Due time
		if (task.due_time != null)
			$("#task-due-time").val(task.due_time);
		
		// Revisions
		var requestedUrl = vineyard.config.serverUrl + "task/" + id + "/revs";
		$.getJSON(requestedUrl, function(revisions) {
			/*revisions = array of {
				end_time: ...,
				worker: ...
			}*/
			$.each(revisions, function (index, rev) {
				var li = '<li><a href="/task/' + taskl.id + '/?rev=' + rev.end_time + '">' +
					rev.end_time + ' (' + S.workers[rev.worker].name + ')</a></li>';
				
				$("#task-revisions ul").append(li);
			});
		})
    });
};

var commitTaskChanges = function () {
	
	var data = $("form").serialize();
	
	if (data != "")
		$.ajax({
			type: 'PUT',
			url: vineyard.config.serverUrl + "task/" + S.task.id,
			data: data
		})
		.fail(showError);
};


var loadTaskModification = function (id) {
	
	var taskRequest = loadTask(id);
	
	$.when(taskRequest).then(function () {
		
		$("#control-ok").on("click", commitTaskChanges);
		
		$("input, select").on("change", function () {
			this.name = this.dataset.name;
			S.controls.css("visibility", "visible");
		});		
	});
};

var init = function() {
	// get id from url
	var id = $("#task-id").val();	
	
	S.controls = $(".controls");
	S.controls.css("visibility", "hidden");
	
	$("#control-cancel").on("click", function () {
		window.location.reload();
	});
	
	if (id == "insert")
		loadTaskInsertion();
	else loadTaskModification(id);
};

init();