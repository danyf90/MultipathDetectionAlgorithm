<?php
namespace Vineyard\Utility;

use \Exception;
use \Vineyard\Utility\AbstractORM;

class Validator {

    const INVALID_FORMAT = "Invalid format";
    const MANDATORY = "Field is mandatory";
    const NON_EXISTANT = "Resource does not exists";
    const INVALID_FIELD = "Field cannot be specified";

    const ABSTRACTORM_FULL_CLASS_NAME = "\\Vineyard\\Utility\\AbstractORM";

    protected $_wrongFields = array();
    protected $object;

    public function __construct(AbstractORM $o, $checkNotSetFields = false) {
        $this->object = $o;
        $this->checkNotSetFields = $checkNotSetFields;
    }

    public function setCheckNotSetFields($bool) {
        $this->checkNotSetFields = (bool) $bool;
    }

    public function addWrongField($field, $message) {
        if (!isset($this->_wrongFields[$field]))
            $this->_wrongFields[$field] = array();

        if (!in_array($message, $this->_wrongFields[$field]))
            $this->_wrongFields[$field][] = $message;
    }

    public function getWrongFields() { return $this->_wrongFields; }

    public function nonNull($fieldName) {
	 // if I must not check not set fields and the given field is not present, it's ok
        if (!$this->checkNotSetFields && !isset($this->object->{$fieldName}))
            return true;

        if (strlen($this->object->{$fieldName}) == 0) {
            $this->addWrongField($fieldName, self::MANDATORY);
            return false;
        }

        return true;
    }

    /**
     * Reroute nullSomething() methods to something() if needed.
     */

    public function __call($method, $args) {
        $fieldName = $args[0];
        // if I must not check not set fields and the given field is not present, it's ok
        if (!$this->checkNotSetFields && !isset($this->object->{$fieldName}))
            return true;

        // if fieldname can be null and actually is null, return true
        if (substr($method, 0, 4) === "null") {
            if (!$this->nonNull($fieldName))
                return true;

            $validatorMethod = strtolower(substr($method, 4));
            return call_user_func_array(array($this, $validatorMethod), $args);
        }

        // if fieldname cannot be null and actually is null, return false
        if (!$this->nonNull($fieldName))
            return false;

        return call_user_func_array(array($this, $method), $args);
    }

    /**
     * ID VALIDATION
     */

    protected function id($fieldName, $resourceName) {
        if (!is_subclass_of($resourceName, self::ABSTRACTORM_FULL_CLASS_NAME))
            throw new Exception($resourceName . " class must extend AbstractORM");

        if (!$resourceName::exists($this->object->{$fieldName})) {
            $this->addWrongField($fieldName, self::NON_EXISTANT);
            return false;
        }

        return true;
    }

    /**
     * TIMESTAMP VALIDATION
     */

    const TIMESTAMP_REGEX = "/([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2})/";

    protected function timestamp($fieldName) {

        $value = $this->object->{$fieldName};

        if (preg_match(self::TIMESTAMP_REGEX, $value) && (strtotime($value) !== FALSE))
            return true;

        $this->addWrongField($fieldName, self::INVALID_FORMAT);
        return false;
    }

    /**
     * NUMERIC VALIDATION
     */
    protected function numeric($fieldName) {
        if (!is_numeric($this->object->{$fieldName})) {
            $this->addWrongField($fieldName, self::INVALID_FORMAT);
            return fase;
        }

        return true;

    }

    /**
     * ENUM VALIDATION
     */

    protected function enum($fieldName, $enumValues) {
	$value = strtolower($this->object->{$fieldName});
        if (!in_array($value, $enumValues)) {
            $this->addWrongField($fieldName, self::INVALID_FORMAT);
            return false;
        }

        return true;
    }

    /**
     * NOT-SET VALIDATOR
     */

    public function notSet($fieldName) {
        if (isset($this->object->{$fieldName})) {
            $this->addWrongField($fieldName, self::INVALID_FIELD);
            return false;
        }

        return true;
    }
}
?>
