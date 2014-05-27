<?php

namespace Vineyard\Utility;

use \PDO;
use \PDOException;
use Vineyard\Utility\DB;
use Vineyard\Utility\ORMException;
use \JsonSerializable;

abstract class TemporalORM extends TrackedORM {

    private function _finalizeSnapshot() {
        $pdo = DB::getConnection();
        $tableName = static::getTableName();

        $query = "UPDATE `" . $tableName . "` SET `end_time` = NOW() WHERE `id` = :id AND `end_time` IS NULL";

        $sql = $pdo->prepare($query);
        $sql->bindValue(":id", $this->_data['id']);

        $sql->execute();
    }

    /**
     * Updates the entry correspondent to this instance in the db.
     */
    protected function _update() {

        $this->_finalizeSnapshot();

        $pdo = DB::getConnection();
        $tableName = static::getTableName();

        $query = "INSERT INTO `" . $tableName . "` (`id`,";
        $query2 = ") VALUES (:id, ";
        foreach ($this->touchedFields as $key) {
            $query .= "`" . $key . "`, ";
            $query2 .= ":" . $key .", ";
        }

        $query = rtrim($query, ", ") . rtrim($query2, ", ") . ")";

        $sql = $pdo->prepare($query);

        $sql->bindValue(":id", $this->_data['id']);
        foreach ($this->touchedFields as $key)
            $sql->bindValue(":" . $key, $this->_data[$key]);

        $sql->execute();
    }

    /**
     * Loads this instance with data from the entry with id $id.
     */
    public function load($id, $rev = "") {
        $pdo = DB::getConnection();
        $tableName = static::getTableName();

        $id = (int) $id;
        $whereParams = array($id);
        $query = "SELECT * FROM `" . $tableName . "` WHERE `id` = ? AND `end_time` ";

        if ($rev == "")
            $query .= "IS NULL";
        else {
            $query .= "= ?";
            $whereParams[] = $rev;
        }

        $sql = $pdo->prepare($query);
        $sql->execute($whereParams);

        $this->_data = $sql->fetch(PDO::FETCH_ASSOC);
        $this->touchedFields = array();
    }
    
    public function populate(array $data) {
        if (isset($data['id']))
            unset($data['id']);

        $this->_data = array_merge($this->_data, $data);
        $this->touchedFields = array_diff( array_keys($this->_data), array("id") );
    }

    /**************************************
     * Utility Methods
     **************************************/

    /**
     * Makes a simple query and executes $scopedFunc on each result object.
     */
    static public function get($scopedFunc, $whereClause = "", $whereParams = array()) {

        if (!is_callable($scopedFunc))
            throw new \Exception("First argument must be callable.");

        $pdo = DB::getConnection();
        $tableName = static::getTableName();

        if($whereClause != "")
            $whereClause = " AND " . $whereClause;

        $query = "SELECT id FROM `" . $tableName . "` WHERE `end_time` IS NULL" . $whereClause;

        $sql = $pdo->prepare($query);
        $sql->execute($whereParams);

        while($id = $sql->fetchColumn()) {
            // TODO check memory alloc/dealloc performances vs memory allocation size
            $s = static::getById($id);
            $scopedFunc($s);
            unset($s);
        }
    }

    public static function getHistoryById($id) {
        $pdo = DB::getConnection();
        $tableName = static::getTableName();

        try {
            $sql = $pdo->prepare("SELECT * FROM `" . $tableName . "` WHERE `id` = ? ORDER BY `start_time` DESC");
            $sql->execute(array($id));
            return $sql->fetchAll(PDO::FETCH_ASSOC);
        }  catch (PDOException $e) {
            http_response_code(400); // Bad Request
            return $e->getMessage() . ": " . implode(",", $e->getWrongFields());
        }
    }

    public static function getByRevision($id, $rev) {
        // TODO check $rev
        if (!is_numeric($id)) {
            http_response_code(400);
            return;
        }

        $s = new static();
        $s->load($id, $rev);

        return $s;
    }


	public static function update($id) {

        // access PUT variables and put them in $_PUT for omogeinity
        parse_str(file_get_contents("php://input"), $_PUT);

        array_walk($_PUT, function(&$v){
            $v = trim($v);
        });

        $s = new static();
        $s->load($id);
		unset($s->start_time);
		unset($s->end_time);
        $s->populate($_PUT);

        try {
            $s->save();
            http_response_code(202); // Accepted
            return ''; // Empty response body
        } catch (ORMException $e) {
            http_response_code(400); // Bad Request
            return $e->getWrongFields();
        } catch (PDOException $e) {
            http_response_code(500);
	    	return $e->getMessage();
        }
    }
    
    // TODO: delete issues? mark as duplicate but maintained?
    /*static public function delete($id) {

        $pdo = DB::getConnection();
        $tableName = static::getTableName();

        try {
            $sql = $pdo->prepare("DELETE FROM `" . $tableName . "` WHERE `id` = ?");
            $id = (int) $id;
            $sql->execute(array($id));

            http_response_code(202); // Accepted
            return ''; // Empty response body
        } catch (PDOException $e) {
            http_response_code(400); // Bad Request
            return $e->getMessage() . ": " . implode(",", $e->getWrongFields());
        }
    }*/

}
?>
