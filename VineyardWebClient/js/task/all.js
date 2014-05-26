// some state
var S = {};

var getTime = function (task) {
	if (task.due_time != null)
		return '<span class="time due">' + task.due_time + '</span>';
	
	if (task.assign_time != null)
		return '<span class="time assign">' + task.assign_time + '</span>';
	
	return '<span class="time create">' + task.create_time + '</span>';
};

var markTaskAsSolved = function () {
	// this = span mark as solved
	$this = $(this);
	var taskId = $this.parent().parent().data("id");
	$.ajax({
		type: 'PUT',
		url: vineyard.config.serverUrl + "task/" + taskId,
		data: {
			modifier: sessionStorage.getItem("workerId"),
			status: 'resolved'
		},
		success: function() {
			
			if ($("#toggle-all").hasClass("active")) {
				// invalidate cache
				S.allTasks = null;
				S.openTasks = null;
				
				$this
				.parent()
					.removeClass("new")
					.removeClass("assigned")
					.addClass("resolved")
				.end()
				.remove();
			}
			else $this.parents("tr").remove();
		}
	});
};

var insertTask = function (task, $container) {
	var row = '<tr data-id="' + task.id + '"><td>' +
		'<a href="/task/' + task.id + '">' + task.title + '</a></td>' + 
		'<td><a href="/place/' + task.place + '">' + S.places[task.place].name + '</a></td>' + 
		'<td class="status ' + task.status + '">' + task.status +
		((task.status != "resolved") ? '<span class="mark-as-solved" title="Mark as Resolved"></span>' : '') +
		'</td>' + 
		'<td class="priority ' + ((task.priority) ? task.priority : "") + '">' + task.priority + '</td>' + 
		'<td>' + getTime(task) + '</td></tr>';
	
	$row = $(row).find(".mark-as-solved").on("click", markTaskAsSolved).end();
	$container.append($row);
};

var insertJSONList = function (tasks) {
	
	$.each(tasks, function (i, task) {
		insertTask(task, S.taskAllTableBody); 
	});
};

var insertElementList = function (tasks) {
	
	for (var i = 0, l = tasks.length; i < l; i++)
		S.taskAllTableBody.append(tasks[i]);
};

var emptyList = function () { S.taskAllTableBody.empty(); };

var sortListBy = function (index, ascending) {
	var sorted = S.taskAllTableBody.children().sort(function(a, b){
		var valA = a.childNodes[index].textContent.toLowerCase();
		var valB = b.childNodes[index].textContent.toLowerCase();	
		var ret = 0;
		
		if (valA > valB)
			ret = 1;
		else if (valA < valB)
			ret = -1;
		
		if (ascending)
			return ret;
		
		return -ret;
	});
	
	emptyList();
	insertElementList(sorted);
};

var loadCompleteList = function () {
	if (S.allTasks != null) {
		insertJSONList(S.allTasks);
		return;
	}
	
	requestedUrl = vineyard.config.serverUrl + "task/" ;
	$.getJSON(requestedUrl, function (tasks) {
		S.allTasks = tasks;
		insertJSONList(tasks);
	});
};

var loadList = function () {
	
    var requestedUrl = vineyard.config.serverUrl + "place/" ;
	$.getJSON(requestedUrl, function (places) {
		S.places = {};
        $.each(places, function (index, place) {
        	S.places[place.id] = place;
        });
		
		requestedUrl = vineyard.config.serverUrl + "task/open/" ;
		$.getJSON(requestedUrl, function (tasks) {
			S.openTasks = tasks;
			insertJSONList(tasks);
			
			$("thead tr").eq(1).children().each(function(i){
				$(this).on("click", function() {
					$this = $(this);
					$this.siblings().removeClass("sorted");
					$this.addClass("sorted");
					$this.toggleClass("ascending");
					sortListBy(i, $this.hasClass("ascending"));
				})
			});
			
			$("#toggle-all").on("click", function() {
				$this = $(this);
				emptyList();
				if (!$this.hasClass("active")) {
					$this.val("show not resolved only");
					loadCompleteList();
				} else {
					$this.val("show all");
					insertJSONList(S.openTasks);
				}
				
				$this.toggleClass("active");
			});
			
		});
    });
};

var init = function () {
	S.taskAllTableBody = $("#task-all-table-body");
	
	loadList();
};

init();

/* TODO LIST
	- add action_up_dark and action_down_dark
*/
