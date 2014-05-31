<form class="content entity-all taskissue-all" id="issue-all">
    <table>
        <thead>
            <tr>
				<th id="loading">Loading...</th>
				<td class="header" colspan="4">
					<div>
						<h2>Issues</h2>
						<input type="button" value="Show All" id="toggle-all">
						<input type="button" value="Show In Map" id="show-issue-map">
					</div>
				</td>
			</tr>
			<tr><th>What</th><th>Where</th><th>Status</th><th>Priority</th><th>When</th><th>by Who</th></tr>
        </thead>
        <tbody id="issue-all-table-body"></tbody>
    </table>
</form>
<div id="issue-map"></div>
<script type="text/javascript" src='/js/maps.js'></script>
<script src="/js/issue/all.js"></script>
