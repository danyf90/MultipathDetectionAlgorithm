<?php
namespace Vineyard\Utility;

class Validator {
    
    const INVALID_FORMAT = "Invalid format";
    const MANDATORY = "Field is mandatory";
    const NON_EXISTANT = "Resource does not exists";
    const INVALID_FIELD = "Field cannot be specified";
    
    protected $_wrongFields = array();
    protected $object;
    
    public function __construct(AbstractORM $o) {
        $object = $o;
    }
    
    public function addWrongField($field, $message) {
        if (!isset($this->_wrongFields[$field]))
            $this->_wrongFields[$field] = array();
        
        if (!in_array($message, $this->_wrongFields[$field]))
            $this->_wrongFields[$field][] = $message;
    }
    
    public function getWrongFields() { return $this->_wrongFields; }
    
    public function nonNull($fieldName) {
        if (strlen($this->o[$fieldName]) == 0) {
            $this->addWrongField($fieldName, self::MANDATORY);
            return false;
        }
        
        return true;
    }
            
    /**
     * ID VALIDATION
     */
            
    public function id($fieldName, $resourceName) {
        if (!$this->nonNull($fieldName))
            return false;
        
        if (!$resourceName instanceof AbstractORM)
            throw Exception($resourceName . " class must extend AbstractORM");
        
        if (!$resourceName::exists($this->o[$fieldName])) {
            $this->addWrongField($fieldName, self::NON_EXISTANT);
            return false;
        }
        
        return true;
    }
            
    public function nullId($fieldName, $resourceName) {
        if (!$this->nonNull($fieldName))
            return true;
        
        if (!$resourceName instanceof AbstractORM)
            throw Exception($resourceName . " class must extend AbstractORM");
        
        if (!$resourceName::exists($this->o[$fieldName])) {
            $this->addWrongField($fieldName, self::NON_EXISTANT);
            return false;
        }
        
        return true;
    }
    
    /**
     * TIMESTAMP VALIDATION
     */
            
    private $timestampRegex = "/([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2})/";
    
    public function timestamp($fieldName) {
        if (!$this->nonNull($fieldName))
            return false;
        
        $value = $this->o[$fieldName];
        
        if (preg_match($timestampRegex, $value) && (strtotime($value) !== FALSE))
            return true;
        
        $this->addWrongField($fieldName, self::INVALID_FORMAT);
        return false;
    }
            
    public function nullTimestamp($fieldName) {
        if (!$this->nonNull($fieldName))
            return true;
        
         $value = $this->o[$fieldName];
        
        if (preg_match($timestampRegex, $value) && (strtotime($value) !== FALSE))
            return true;
        
        $this->addWrongField($fieldName, self::INVALID_FORMAT);
        return false;
    }
    
    /**
     * NUMERIC VALIDATION
     */
    public function numeric($fieldName) {
        if (!$this->nonNull($fieldName))
            return false;

        if (!is_numeric($this->o[$fieldName])) {
            $this->addWrongField($fieldName, self::INVALID_FORMAT);
            return fase;
        }
        
        return true;
            
    }        
            
    public function nullNumeric($fieldName) {
        if (!$this->nonNull($fieldName))
            return true;
        
        if (!is_numeric($this->o[$fieldName])) {
            $this->addWrongField($fieldName, self::INVALID_FORMAT);
            return false;
        }
        
        return true;
    }
    
    /**
     * ENUM VALIDATION
     */
            
    public function enum($fieldName, $enumValues) {
        if (!$this->nonNull($fieldName))
            return false;
        
        if (!in_array($this->o[$fieldName], $enumValues)) {
            $this->addWrongField($fieldName, self::INVALID_FORMAT);
            return false;
        }
        
        return true;
    }
            
    public function nullEnum($fieldName, $enumValues) {
        if (!$this->nonNull($fieldName))
            return true;
        
        if (!in_array($this->o[$fieldName], $enumValues)) {
            $this->addWrongField($fieldName, self::INVALID_FORMAT);
            return false;
        }
        
        return true;
    }
    
    /**
     * NOT-SET VALIDATOR
     */
    
    public function notSet($fieldName) {
        if (isset($this->o[$fieldName])) {
            $this->addWrongField($fieldName, self::INVALID_FIELD);
            return false;
        }
        
        return true;
    }
}
?>