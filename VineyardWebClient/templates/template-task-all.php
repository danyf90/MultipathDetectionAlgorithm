<form class="content entity-all taskissue-all" id="task-all">
    <table>
        <thead>
            <tr>
				<th id="loading">Loading...</th>
				<td class="header" colspan="4">
					<div>
						<h2>Tasks</h2>
						<a href="/task/insert"><input type="button" value="Add a task"></a>
						<input type="button" value="show all" id="toggle-all">
					</div>
				</td>
			</tr>
			<tr><th>What</th><th>Where</th><th>Status</th><th>Priority</th><th>When</th></tr>
        </thead>
        <tbody id="task-all-table-body"></tbody>
		<tfoot id="task-all-add-task">
			<tr><td></td><td class="empty"><a href="/task/insert">+ Add a task...</a></td><td></td></tr>
		</tfoot>
    </table>
	<script src="/js/task/all.js"></script>
</form>