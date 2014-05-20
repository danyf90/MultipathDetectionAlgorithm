var insertPlace = function (place, $container, level) {
	
	var hasChildren = (place.children && place.children.length > 0);
	var childrenClass = (hasChildren) ? ' class="has-children"' : '';
	var numIssues = place.issues || 0;
	var numTasks = place.tasks || 0;
	var padding = 10 + level * 20;
	
	var row = '<tr' + childrenClass +
		'><td class="first-col" style="padding-left: ' + padding + 'px;">' +
		'<a href="/place/' + place.id + '">' + place.name + '</a></td>' + 
		'<td>' + place.description + '</td>' + 
		'<td class="third-col"><span class="issues">' + numIssues + '</span>' + 
			'<span class="tasks">' + numTasks + '</span></td></tr>';
	
	$container.append(row);
	
	if (hasChildren) {
		
		var childrenHTML = '<tr class="children-row"><td colspan="3"><table class="inner-table"></table></td></tr>';
		$container.append(childrenHTML);
		var childrenContainer = $container.find(".inner-table").last();
		
		for (var i = 0, l = place.children.length; i < l; i++)
			insertPlace(place.children[i], childrenContainer, level+1);
	}
};

var loadList = function () {
    
    var requestedUrl = vineyard.config.serverUrl + "place/hierarchy/" ;
	
    $.getJSON(requestedUrl, function (hierarchy) {
		insertPlace(hierarchy, $("#place-all-table-body"), 0);
		
		$("#place-all tr").not(".children-row").on("click", function () {
			$(this).toggleClass("open").next(".children-row").children().toggle();
		});
		
		$("#place-all .children-row > td").hide();
		
		$("#loading").css("visibility", "hidden"); 
    });
};

loadList();