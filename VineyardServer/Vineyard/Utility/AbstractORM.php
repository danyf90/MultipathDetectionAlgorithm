<?php

namespace Vineyard\Utility;

use \PDO;
use \PDOException;
use \Vineyard\Utility\DB;
use \Vineyard\Utility\ORMException;
use \JsonSerializable;

abstract class AbstractORM implements JsonSerializable {

    /***************************************
     * Methods to be implemented by the user
     **************************************/

    /**
     * Checks if data in this ORM object is valid to be put in database.
     * This method must return an array containing the invalid fields as keys and an error description as value.
     * An empty array is returned if the instance is valid and can be put in the database.
     * @return array
     */
    abstract public function check();

    /**
     * Must return the table name containing data for this type.
     * @return string
     */
    abstract public static function getTableName();

     /**************************************
     * Accessors Methods
     **************************************/

    protected $_data = array();
    protected $touchedFields = array();

    public function __get($key) {
        if (array_key_exists($key, $this->_data))
            return $this->_data[$key];
        return null;
    }

    public function __set($key, $value) {
        // impedire accesso a ID!
        if ($key == "id")
            return;

        $this->_data[$key] = $value;
        $this->touchedFields[] = $key;
    }

    public function __isset($key)
    {
        return isset($this->_data[$key]);
    }

    public function __unset($key) {
        // impedire accesso a ID!
        if ($key == "id")
            return;

        unset($this->_data[$key]);
    }

    /**************************************
     * Savers and Loaders Methods
     **************************************/

    /**
     * Save the instance in the db, either inserting or updating an entry.
     */
    public function save() {

        $wrong_fields = $this->check();

        if (!empty($wrong_fields))
            throw new ORMException($wrong_fields);

        if (isset($this->_data['id']))
            $this->_update();
        else $this->_insert();

        return;

    }

    /**
     * Overridable event, triggered before the instance is inserted in the DB.
     */
    protected function onPreInsert() {}

    /**
     * Inserts a new entry corresponding to this instance in the db.
     */
    private function _insert() {

        $this->onPreInsert();

        $pdo = DB::getConnection();
        $tableName = static::getTableName();

        $query = "INSERT INTO `" . $tableName . "` (`id`,";
        $query2 = ") VALUES (DEFAULT, ";
        foreach ($this->touchedFields as $key) {
            $query .= "`" . $key . "`, ";
            $query2 .= ":" . $key .", ";
        }

        $query = rtrim($query, ", ") . rtrim($query2, ", ") . ")";

        $sql = $pdo->prepare($query);

        foreach ($this->touchedFields as $key)
            $sql->bindValue(":" . $key, $this->_data[$key]);

        $sql->execute();

        $this->_data['id'] = $pdo->lastInsertId();

        $this->onPostInsert();
    }

    /**
     * Overridable event, triggered after the instance is inserted in the DB.
     */
    protected function onPostInsert() {}

    /**
     * Updates the entry correspondent to this instance in the db.
     */
    private function _update() {

        $pdo = DB::getConnection();
        $tableName = static::getTableName();

        $query = "UPDATE `" . $tableName . "` SET ";
        foreach ($this->touchedFields as $key)
            $query .= "`" . $key . "` = :" . $key . ", ";

        $query = rtrim($query, ", ") . " WHERE `id` = :id";

        $sql = $pdo->prepare($query);

        foreach ($this->touchedFields as $key)
            $sql->bindValue(":" . $key, $this->_data[$key]);
        $sql->bindValue(":id", $this->_data['id']);

        $sql->execute();
    }

    /**
     * Loads this instance with data from the entry with id $id.
     */
    public function load($id) {
        $pdo = DB::getConnection();
        $tableName = static::getTableName();

        $sql = $pdo->prepare("SELECT * FROM `" . $tableName . "` WHERE `id` = ?");
        $id = (int) $id;
        $sql->execute(array($id));

        $this->_data = $sql->fetch(PDO::FETCH_ASSOC);
        $this->touchedFields = array();
    }

    /**
     * Populates this instance with $data.
     */
    public function populate(array $data) {
        if (isset($data['id']))
            unset($data['id']);

        $this->_data = array_merge($this->_data, $data);
        $this->touchedFields = array_keys($data);
    }

    /**
     * By default, JSON-encoding encodes only database data.
     */
    public function jsonSerialize() {
        return array_filter($this->_data, function($prop) {
            return !is_null($prop);
        });
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
            $whereClause = " WHERE " . $whereClause;

        $query = "SELECT id FROM `" . $tableName . "` " . $whereClause;

        $sql = $pdo->prepare($query);
        $sql->execute($whereParams);

        while($id = $sql->fetchColumn()) {
            // TODO check memory alloc/dealloc performances vs memory allocation size
            $s = static::getById($id);
            $scopedFunc($s);
            unset($s);
        }
    }

    static public function getById($id) {
        if (!is_numeric($id)) {
            http_response_code(400);
            return;
        }

        $s = new static();
        $s->load($id);

        return $s;
    }

    static public function listAll() {

        $list = array();

        static::get(function($obj) use (&$list) {
            $list[] = clone $obj;
        });

        return $list;
    }

    static public function exists($id) {
        $pdo = DB::getConnection();
        $tableName = static::getTableName();

        $sql = $pdo->prepare("SELECT 1 FROM `" . $tableName . "` WHERE `id` = ?");
        $id = (int) $id;
        $sql->execute(array($id));

        if ($sql->rowCount() > 0)
            return true;
        return false;
    }

    static public function insert() {

        array_walk($_POST, function(&$v){
            $v = trim($v);
        });

        $s = new static();
        $s->populate($_POST);

        try {
            $s->save();
            http_response_code(201); // Created
            return array( 'id' => $s->id );
        } catch (ORMException $e) {
            http_response_code(400); // Bad Request
            return $e->getWrongFields();
        }
    }

    static public function update($id) {

        // access PUT variables and put them in $_PUT for omogeinity
        parse_str(file_get_contents("php://input"), $_PUT);

        array_walk($_PUT, function(&$v){
            $v = trim($v);
        });

        $s = new static();
        $s->load($id);
        $s->populate($_PUT);

        try {
            $s->save();
            http_response_code(202); // Accepted
            return ''; // Empty response body
        } catch (ORMException $e) {
            http_response_code(400); // Bad Request
            return $e->getWrongFields();
        }
    }

    static public function delete($id) {

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
            return $e->getWrongFields();
        }
    }
}
?>
