<?php

namespace Vineyard\Model;

use \PDO;
use \PDOException;
use \Vineyard\Utility\DB;
use \Vineyard\Utility\IResource;
use \Vineyard\Utility\Validator;
use \Vineyard\Utility\TrackedORM;
use \Vineyard\Utility\TCrudRequestHandlers;

class Worker extends TrackedORM implements IResource {

    use TCrudRequestHandlers; // introduces handleRequestToBaseUri() and handleRequestToUriWithId()

    public function check() {
        $v = new Validator($this);

        $v->nonNull('username');
        $v->nullEmail('email');
        $v->notSet('password');

        return $v->getWrongFields();
    }
    public static function getTableName() { return 'worker'; }

    public static function handleRequest($method, array $requestParameters) {

        switch (count($requestParameters)) {
            case 0: // api/worker/
                return static::handleRequestsToBaseUri($method, $requestParameters);
            break;

            case 1: // api/worker/<id> or api/worker/login

                switch ($requestParameters[0]) {
                    case "login":
                        if ($method != "POST") {
            				if ($method == "OPTIONS") {
			     				header("Allow: GET");
								return;
							}
							
                            http_response_code(501); // Not implemented
                        }

                        return static::handleLoginRequest($method);
                    break;

                    default: // we hope is <id>
                        return static::handleRequestsToUriWithId($method, $requestParameters);
                }
            break;

            case 2: // not implemented yet

                $id = array_shift($requestParameters);

                switch($requestParameters[0]) {

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

        $pdo = DB::getConnection();

        try {
            if (array_key_exists('email', $_POST))
                $idField = 'email';
            else
                $idField = 'username';

            $sql = $pdo->prepare("SELECT id FROM `worker` WHERE `" . $idField . "` = ? AND `password` = ?");
            $sql->execute(array($_POST[$idField], $_POST['password']));

             if ($sql->rowCount() > 0)
             {
                 $response = array( 'id' => $sql->fetchColumn(0) );
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

}

?>
