<?php

namespace Vineyard\Model;

use \PDO;
use \Vineyard\Utility\DB;
use \Vineyard\Utility\IResource;

use \Vineyard\Model\Photo;
use \Vineyard\Model\AbstractTask;

class Issue extends AbstractTask implements IResource {

	static public function isIssueInstance() { return true; }
	
    public function check() {
        $v = parent::check();
		$modelNamespace = "\\Vineyard\\Model\\";

        // issuer
        $v->id('issuer', $modelNamespace . "Worker");

        return $v->getWrongFields();
    }


    // Override Task::onPostInsert()
    protected function onPostInsert() {
			parent::onPostInsert();
		    // Send notifications
			$this->notify("issue-insertion");
    }

    static public function getById($id) {
		$s =  parent::getById($id);
		$s->loadPhotos();
		return $s;
	}
	
	protected function onPostUpdate() {
		$this->nofity("task-modification");
	}

    public static function handleRequest($method, array $requestParameters) {

        switch (count($requestParameters)) {
            case 0: // api/issue/
                return static::handleRequestsToBaseUri($method, $requestParameters);
            break;

            case 1: // api/issue/<id>
                if (!is_numeric($requestParameters[0]))
                    return static::handlePerStatusRequest($method, $requestParameters[0]);

                return static::handleRequestsToUriWithId($method, $requestParameters);
            break;

            case 2: // api/issue/<id>/photo or api/issue/<id>/revs/

                $id = array_shift($requestParameters);
                $service = array_shift($requestParameters);

                switch ($service) {

                    case "photo":
                        switch ($method) {
                            // the only one implemented
                            case "POST":
                                return static::insertPhoto($id);
                            break;

                            // case "DELETE": you must give me the url (filename) of the photo to delete
                            // case "GET": not implemented, all attributes are already returned with a task resource
                            // case "PUT": not implemented, delete and recreate it
                            default:
                                http_response_code(501); // Not Implemented
                                return;
                        }
                    break;

                    case "revs":
                        return static::handleRevisionsRequest($method, $id);
                    break;

                    default:
                        http_response_code(400); // Bad Request
                }

                $id = array_shift($requestParameters);


            break;

            case 3: //api/issue/<id>/photo/<filename>
                if ($requestParameters[1] != "photo") {
                    http_response_code(400); // Bad Request
                    return;
                }

                $id = array_shift($requestParameters);
                /* "photo" = */ array_shift($requestParameters);
                $filename = array_shift($requestParameters);

                switch ($method) {

                    case "DELETE":
                        return static::deletePhoto($id, $filename);
                    break;

                    case "POST": // cannot create with given filename: bad request
                        http_response_code(400); // Bad Request
                        return;
                    break;

                    case "OPTIONS":
                         header("Allow: DELETE,POST");
                    break;

                    // case "PUT": not implemented, delete and recreate it!
                    // case "GET": implemented in "Photo" resource
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

    /**
     * PHOTO MANAGEMENT
     */
    public function loadPhotos() {
        if (!isset($this->id))
            return;

        $pdo = DB::getConnection();

        try {
            $sql = $pdo->prepare("SELECT `url` FROM `task_photo` WHERE `task` = ?");
            $sql->execute(array($this->id));

            if ($sql->rowCount() > 0)
                $this->photos = $sql->fetchAll(PDO::FETCH_COLUMN, 0);

        } catch (PDOException $e) {
            // check which SQL error occured
            switch ($e->getCode()) {
                default:
                    http_response_code(400); // Bad Request
            }
        }
    }

    public static function insertPhoto($id) {

        $t = new static();
        $t->loadEmpty($id);
        if (!$t->isIssue()) {
            http_response_code(403); // Forbidden
            return;
        }

        if (!isset($_FILES['photo'])) {
            http_response_code(400); // Bad Request
            return;
        }

        // same interface of an upload
        $filename = "i" . time() . "_" . $id;
        $filepath = Photo::getFullPhotoPath($filename);

        if (move_uploaded_file($_FILES['photo']['tmp_name'], $filepath)) {

            $pdo = DB::getConnection();
            try {

                $sql = $pdo->prepare("INSERT INTO `task_photo` (`task`, `url`) VALUES (?, ?)");
                $sql->execute(array($id, $filename));

                http_response_code(201); // Created
                static::updateLastModified();
                return json_encode(array('url' => $filename));

            } catch (PDOException $e) {
                // check which SQL error occured
                switch ($e->getCode()) {
                    default:
                        http_response_code(400); // Bad Request
                }

                unlink($filepath);
            }
        } else {
            http_response_code(500); // Internal Server Error
            return;
        }

    }

    public static function deletePhoto($id, $filename) {

        $filepath = Photo::getFullPhotoPath($filename);

        if (file_exists($filepath)) {

            $pdo = DB::getConnection();

            try {
                $sql = $pdo->prepare("DELETE FROM `task_photo` WHERE `task` = ? AND `url` = ?");
                $sql->execute(array($id, $filename));

                if ($sql->rowCount() == 1) {
                    http_response_code(202); // Accepted
                    static::updateLastModified();
                }
                else http_response_code(406); // Not Acceptable

                unlink($filepath);
                return;
            } catch (PDOException $e) {
                // check which SQL error occured
                switch ($e->getCode()) {
                    default:
                    http_response_code(400); // Bad Request
                }
            }
        } else
            http_response_code(400); // Bad Request
    }
	
	
}

?>
