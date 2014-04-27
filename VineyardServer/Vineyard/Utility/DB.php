<?php
namespace Vineyard\Utility;

use \PDO;

class DB {
		
	private static $credentials = null;
	
	private static $instance = null;
	
	public static function getConnection($credentialsFile = null) {
		
		if (is_null(self::$instance)) {
			
			if (is_null($credentialsFile))
				$credentialsFile = dirname(__FILE__) . "/../config.db.php";
			
			self::$credentials = include($credentialsFile);
			
			$db_uri = 'mysql:host=' . self::$credentials['host'] . ';dbname=' . self::$credentials['db_name'];
			self::$instance = new PDO($db_uri , self::$credentials['user'], self::$credentials['password']);
			self::$instance->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
		}
		
		return self::$instance;
	}
}

?>
