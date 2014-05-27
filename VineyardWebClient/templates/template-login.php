<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title><?php $this->title = "Login - Vineyard Web Client"; ?></title>
        <link href="/style.css" rel="stylesheet" type="text/css" />
        <script src="/js/md5.js"></script>
        <script src="/js/jquery.min.js"></script>
    </head>
    <body>
        <header>
            <h1><a href="/">Vineyard</a></h1>
			<ul><li><span id="loading" class="login-loading">Logging in...</span></li></ul>
        </header>
        <main class="login-contents">
            <form action="/" method="post" id="login-form">
				<h1>Welcome!</h1>
				<p>Welcome to Vineyard Web Client login.</p>
				<p>You can login using either the usename or the email address provided to the system.</p>
				<input class="ajax" type="text" name="username" placeholder="Username or Email" autofocus="true" />
				<input class="ajax" id="password" type="password" name="password" placeholder="Password" />
				<input class="ajax" type="text" id="server" placeholder="Vineyard Server Address" value="http://vineyard-server.no-ip.org/"/>
				<input class="ajax" type="hidden" name="role" value="admin"/>
				<input type="submit" value="Login" />
				<input type="hidden" id="login-id"/>
			</form>
        </main>
        <footer>
            <p>Vineyard Web Client. All rights reserved &copy; TrainSoft Inc.</p>
        </footer>
		<script src="js/login.js"></script>
    </body>
</html>
