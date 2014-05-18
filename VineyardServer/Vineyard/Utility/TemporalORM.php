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
    private function _update() {

        $this->_finalizeSnapshot();

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
    public function load($id) {
        $pdo = DB::getConnection();
        $tableName = static::getTableName();

        $sql = $pdo->prepare("SELECT * FROM `" . $tableName . "` WHERE `id` = ? AND `end_time` IS NULL");
        $id = (int) $id;
        $sql->execute(array($id));

        $this->_data = $sql->fetch(PDO::FETCH_ASSOC);
        $this->touchedFields = array();
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
