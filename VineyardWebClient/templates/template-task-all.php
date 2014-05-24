<form class="content entity-all" id="task-all">
	
    <table>
        <thead>
            <tr>
				<th id="loading">Loading...</th>
				<td class="header" colspan="4">
					<div>
						<h2>Tasks</h2>
						<a href="/task/insert"><input type="button" value="Aggiungi un lavoro"></a>
						<input type="button" value="Mostra tutti" id="toggle-all">
					</div>
				</td>
				
			</tr>
			<tr><th>What</th><th>Where</th><th>Status</th><th>Priority</th><th>When</th></tr>
        </thead>
        <tbody id="task-all-table-body">
			<tr><td>A</td><td>B</td><td class="status">new<span class="mark-as-solved"></span></td><td>high</td><td>ora</td></tr>
			<tr><td>B</td><td>A</td><td>assigned</td><td>low</td><td>ieri</td></tr>
        </tbody>
		<tfoot id="task-all-add-task">
			<tr><td></td><td class="empty"><a href="/task/insert">+ Add a task...</a></td><td></td></tr>
		</tfoot>
    </table>
	<script src="/js/task/all.js"></script>
</form>