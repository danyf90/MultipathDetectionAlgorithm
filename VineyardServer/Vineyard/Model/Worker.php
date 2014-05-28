<?php

namespace Vineyard\Model;

use \PDO;
use \PDOException;
use \Vineyard\Utility\DB;
use \Vineyard\Utility\IResource;
use \Vineyard\Utility\Validator;
use \Vineyard\Utility\ORMException;
use \Vineyard\Utility\TrackedORM;
use \Vineyard\Utility\TCrudRequestHandlers;

class Worker extends TrackedORM implements IResource {

    use TCrudRequestHandlers; // introduces handleRequestToBaseUri() and handleRequestToUriWithId()

    public function check() {
        $v = new Validator($this, !isset($this->id));

        $v->nonNull('username');
        $v->nullEmail('email');
        // $v->notSet('password');

        return $v->getWrongFields();
    }

    public static function getTableName() { return 'worker'; }

    // Override of AbstarctORM::getById()
    public static function getById($id) {
        $w = parent::getById($id);
        unset($w->password);
        unset($w->notification_id);
		return $w;
    }

    protected function generatePassword() {
	    // TODO
    	$this->password = '5f4dcc3b5aa765d61d8327deb882cf99';
    }

    protected function sendPasswordByEmail() {
	    // TODO
    }

    // Override AbstractORM::insert()
	public static function insert() {
		unset($_POST['password']);
        array_walk($_POST, function(&$v){
            $v = trim($v);
        });

        $s = new static();
        $s->populate($_POST);
        $s->generatePassword();

        try {
            $s->save();
            http_response_code(201); // Created
            $s->sendPasswordByEmail();
            return array( 'id' => $s->id );
        } catch (ORMException $e) {
            http_response_code(400); // Bad Request
            return $e->getWrongFields();
        } catch (PDOException $e) {
			http_response_code(400);
			return $e->getMessage();
		}
    }

    public static function handleRequest($method, array $requestParameters) {

        switch (count($requestParameters)) {
            case 0: // api/worker/
                return static::handleRequestsToBaseUri($method, $requestParameters);
            break;

            case 1: // api/worker/<id> or api/worker/login

                switch ($requestParameters[0]) {
                    case "login":
                        return static::handleLoginRequest($method);
                    break;

                    default: // we hope is <id>
                        return static::handleRequestsToUriWithId($method, $requestParameters);
                }
            break;

            case 2: // api/worker/<id>/logout

                $id = array_shift($requestParameters);

                switch($requestParameters[0]) {
		    case "logout":
			return static::handleLogoutRequest($method, $id);
		    break;

                    default:
                        http_response_code(501); // Not Implemented
                        return;
                }
            break;

            default:
                http_response_code(501); // Not Implemented
                return;

        }
    }

     /**************************
     * LOGIN HANDLING
     **************************/

    public static function handleLoginRequest() {
	if ($method == "OPTIONS") {
        	header("Allow: POST");
		return;
 	}

	if ($method != "POST") {
		http_response_code(501); // Not implemented
		return;
        }

        $pdo = DB::getConnection();

        try {
            if (array_key_exists('email', $_POST))
                $idField = 'email';
            else
                $idField = 'username';

            $sql = $pdo->prepare("SELECT `id`, `name` FROM `worker` WHERE `" . $idField . "` = ? AND `password` = ? AND FIND_IN_SET(?, `role`) > 0;");
            $sql->execute(array($_POST[$idField], $_POST['password'], strtolower($_POST['role'])));

             if ($sql->rowCount() > 0)
             {
                 $response = $sql->fetch(PDO::FETCH_ASSOC);
                 http_response_code(202); // Accepted
                 return json_encode($response);
             }

             http_response_code(401); // Not Authorized
             return;
        } catch (PDOException $e) {
            // check which SQL error occured
            switch ($e->getCode()) {
                default:
                    http_response_code(400); // Bad Request
            }
        }
    }

    public static function handleLogoutRequest($method, $id) {
	if ($method == "OPTIONS") {
	    header("Allow: POST");
	    return;
	}

	if ($method != "POST") {
	    http_response_code(501); // Not Implemented
	    return;
	}

	$w = static::getById($id);
	if ($w == null) {
	    http_response_code(400); // Bad Request
	    return;
	}

	$w->notification_id = null;
	$w->save();
    }

}

?>
