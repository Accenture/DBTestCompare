      SELECT  
      pr.[StoreNumber]
      ,pr.ProductID
      ,pr.[Date]
      ,pr.[Quantity]
  FROM [dbo].[TemplateKeyComparatorProducts] pr
  inner join AdventureWorks2008R2.dbo.TemplateKeyComparatorCheckAggregators d 
  on d.[StoreNumber]=pr.[StoreNumber] and d.ProductID=pr.ProductID