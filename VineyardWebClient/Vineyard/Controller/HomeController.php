<?php

namespace Vineyard\Controller;

use \Vineyard\Utility\Template;

class HomeController implements IController {
    public static function handle(Template $t, array $requestParams) {
		
		static::checkLoginLogout();
		
		if (!static::isLoggedIn()) {
			$t = new Template("templates/template-login.php");
			return $t;
		}
		
		$t->content = '<div style="padding: 40px;">';
        $t->content .= '<h2>Welcome to Vineyard Web Client!</h2>';
		$t->content .= '<p>This page is currently empty, choose something in the menu on the left.</p>';
		$t->content .= '</div>';
		return $t;
    }
	
	protected static function checkLoginLogout() {
		session_start();
		
		if (isset($_POST['login']))
			$_SESSION['logged'] = (int) $_POST['login'];
		
		else if (isset($_GET['logout']))
			unset($_SESSION['logged']);
	}
	
	protected static function isLoggedIn() {
		return isset($_SESSION['logged']);
	}
}

?>