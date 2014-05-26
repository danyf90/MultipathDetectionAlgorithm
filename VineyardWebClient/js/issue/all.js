// some state
var S = {};

var getTime = function (issue) {
	if (issue.due_time != null)
		return '<span class="time due">' + issue.due_time + '</span>';
	
	if (issue.assign_time != null)
		return '<span class="time assign">' + issue.assign_time + '</span>';
	
	return '<span class="time create">' + issue.create_time + '</span>';
};

var markIssueAsSolved = function () {
	// this = span mark as solved
	$this = $(this);
	var issueId = $this.parent().parent().data("id");
	$.ajax({
		type: 'PUT',
		url: vineyard.config.serverUrl + "issue/" + issueId,
		data: {
			modifier: sessionStorage.getItem("workerId"),
			status: 'resolved'
		},
		success: function() {
			
			if ($("#toggle-all").hasClass("active")) {
				// invalidate cache
				S.allIssues = null;
				S.openIssues = null;
				
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

var insertIssue = function (issue, $container) {
	var row = '<tr data-id="' + issue.id + '"><td>' +
		'<a href="/issue/' + issue.id + '">' + issue.title + '</a></td>' + 
		'<td><a href="/place/' + issue.place + '">' + S.places[issue.place].name + '</a></td>' + 
		'<td class="status ' + issue.status + '">' + issue.status +
		((issue.status != "resolved") ? '<span class="mark-as-solved" title="Mark as Resolved"></span>' : '') +
		'</td>' + 
		'<td class="priority ' + ((issue.priority) ? issue.priority : "") + '">' + issue.priority + '</td>' + 
		'<td>' + getTime(issue) + '</td>' + 
		'<td><a href="/worker/' + issue.issuer + '">' + S.workers[issue.issuer].name + '</a></td></tr>';
	
	$row = $(row).find(".mark-as-solved").on("click", markIssueAsSolved).end();
	$container.append($row);
};

var insertJSONList = function (issues) {
	
	$.each(issues, function (i, issue) {
		insertIssue(issue, S.issueAllTableBody); 
	});
};

var insertElementList = function (issues) {
	
	for (var i = 0, l = issues.length; i < l; i++)
		S.issueAllTableBody.append(issues[i]);
};

var emptyList = function () { S.issueAllTableBody.empty(); };

var sortListBy = function (index, ascending) {
	var sorted = S.issueAllTableBody.children().sort(function(a, b){
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
	if (S.allIssues != null) {
		insertJSONList(S.allIssues);
		return;
	}
	
	requestedUrl = vineyard.config.serverUrl + "issue/" ;
	$.getJSON(requestedUrl, function (issues) {
		S.allIssues = issues;
		insertJSONList(issues);
	});
};

var loadWorkers = function () {
	var requestedUrl = vineyard.config.serverUrl + "worker/";
	return $.getJSON(requestedUrl, function(workers) {
		S.workers = {};
		 $.each(workers, function (index, worker) {
        	S.workers[worker.id] = worker;
        });
	});
};

var loadPlaces = function () {
	var requestedUrl = vineyard.config.serverUrl + "place/";
	return $.getJSON(requestedUrl, function (places) {
		S.places = {};
        $.each(places, function (index, place) {
        	S.places[place.id] = place;
        });
	});
};


var loadList = function () {
	
    var requestedUrl = vineyard.config.serverUrl + "place/" ;
	
	var workersRequest = loadWorkers();
	var placesRequest = loadPlaces();
	
	$.when( placesRequest, workersRequest ).done(function () {
		var requestedUrl = vineyard.config.serverUrl + "issue/open/" ;
		$.getJSON(requestedUrl, function (issues) {
			S.openIssues = issues;
			insertJSONList(issues);
			
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
					insertJSONList(S.openIssues);
				}
				
				$this.toggleClass("active");
			});
			
		});
	});
		
};

var init = function () {
	S.issueAllTableBody = $("#issue-all-table-body");
	
	loadList();
};

init();