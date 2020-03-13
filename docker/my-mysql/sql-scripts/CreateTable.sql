CREATE TABLE IF NOT EXISTS personaddress (
  AddressID int(11) DEFAULT NULL,
  AddressLine1 text,
  City text,
  StateProvinceID int(11) DEFAULT NULL,
  PostalCode int(11) DEFAULT NULL,
  ModifiedDate text
);

CREATE TABLE IF NOT EXISTS salesorderdetail (
  SalesOrderID int(11) DEFAULT NULL,
  SalesOrderDetailID int(11) DEFAULT NULL,
  OrderQty int(11) DEFAULT NULL,
  ProductID int(11) DEFAULT NULL,
  SpecialOfferID int(11) DEFAULT NULL,
  UnitPrice double DEFAULT NULL,
  UnitPriceDiscount double DEFAULT NULL,
  LineTotal double DEFAULT NULL
);