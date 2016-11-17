SELECT SalesOrderID
      ,SalesOrderDetailID
      ,CarrierTrackingNumber
      ,OrderQty
      ,ProductID
      ,SpecialOfferID
      ,UnitPrice
      ,UnitPriceDiscount
      ,LineTotal
      ,rowguid
      ,ModifiedDate
  FROM AdventureWorks2008R2.Sales.SalesOrderDetail
  order by SalesOrderID;