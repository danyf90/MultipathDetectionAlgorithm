<?php

namespace Vineyard\Utility;

use \PDO;
use \PDOException;
use Vineyard\Utility\DB;
use Vineyard\Utility\ORMException;
use \JsonSerializable;

abstract class TrackedORM extends AbstractORM {

    protected static function updateLastModified() {

        $pdo = DB::getConnection();
        $tableName = static::getTableName();

        $query = "INSERT INTO `last_modified` (`table_name`, `timestamp`)
            VALUES ('" . $tableName . "', NOW())
            ON DUPLICATE KEY UPDATE `timestamp` = NOW()";

        $pdo->query($query);
    }

    public static function lastModified() {
        $pdo = DB::getConnection();
        $tableName = static::getTableName();
        $sql = $pdo->prepare("SELECT `timestamp` FROM `last_modified` WHERE `table_name` = ?");
        $sql->execute(array($tableName));

        return $sql->fetchColumn(0);
    }

    /**
     * OVERRIDES
     */

    public function save() {
        parent::save();
        static::updateLastModified();
    }

     public static function delete($id) {
         $response = parent::delete($id);

         if (http_response_code() == 202) // Accepted
             static::updateLastModified();

         return $response;
     }

}

?>
