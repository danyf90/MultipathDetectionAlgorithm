<?php

namespace Vineyard\Utility;

trait TGetSet {
    protected $_data = [];
    public function __get($key) {
        if (array_key_exists($key, $this->_data))
            return $this->_data[$key];
        
        throw new \Exception("No property '$key' found in " . __CLASS__);
    }
    
    public function __set($key, $value) {
        $this->_data[$key] = $value;
    }
    
    public function jsonSerialize() {
        return $this->_data;
    }
}

?>