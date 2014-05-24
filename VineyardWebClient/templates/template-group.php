<form class="content entity" id="group">
	<input type="hidden" name="id" id="group-id" value="<?php $this->id; ?>"/>
    <table>
        <thead>
            <tr><th id="loading">Loading...</th><td><h2><input id="group-name" placeholder="Insert a name..." type="text" /></h2></td></tr>
        </thead>
        <tbody id="group-table-body">
            <tr><th>Description</th><td><input id="group-description" type="text" placeholder="Insert the description..."/></td></tr>
            <tr><th>Workers</th><td><input id="group-workers" type="text" placeholder="Insert the workers..."/></td></tr>
        </tbody>
    </table>
    <script src="/js/group/single.js"></script>
</form>