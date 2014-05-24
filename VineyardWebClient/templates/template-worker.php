<form class="content entity" id="worker">
	<input type="hidden" name="id" id="worker-id" value="<?php $this->id; ?>"/>
    <table>
        <thead>
            <tr><th id="loading">Loading...</th><td><h2><input id="worker-name" placeholder="Insert a name..." type="text" /></h2></td></tr>
        </thead>
        <tbody id="worker-table-body">
            <tr><th>Username</th><td><input id="worker-username" type="text" placeholder="Insert the username..."/></td></tr>
            <tr><th>Email</th><td><input id="worker-username" type="text" placeholder="Insert the email..."/></td></tr>
            <tr><th>Role</th><td><input id="worker-role" type="text" placeholder="Insert the roles..."/></td></tr>
        </tbody>
    </table>
    <script src="/js/worker/single.js"></script>
</form>