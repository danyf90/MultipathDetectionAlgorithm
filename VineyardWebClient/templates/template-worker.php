<form class="content entity" id="worker">
	<input type="hidden" name="id" id="worker-id" value="<?php $this->id; ?>"/>
    <table>
        <thead>
            <tr><th id="loading">Loading...</th><td><h2><input id="worker-name" data-name="name" placeholder="Insert a name..." type="text" /></h2></td></tr>
        </thead>
        <tbody id="worker-table-body">
            <tr><th>Username</th><td><input id="worker-username" data-name="username" type="text" placeholder="Insert the username..."/></td></tr>
            <tr><th>Email</th><td><input id="worker-email" data-name="email" type="text" placeholder="Insert the email..."/></td></tr>
            <tr><th>Roles</th><td><select id="worker-role" multiple="multiple">
					<option value="admin">Administrator</option>
					<option value="operator">Operator</option>
				</select>
				<input type="hidden" data-name="role" />
				</td></tr>
        </tbody>
		<tfoot>
			<tr><th></th><td class="controls"><input type="button" id="control-ok" value="Done" /> <input type="button" id="control-cancel" value="Cancel"/></td></tr>
		</tfoot>
    </table>
    <script src="/js/worker/single.js"></script>
</form>