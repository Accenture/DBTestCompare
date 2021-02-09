SELECT [ContactTypeID]
      ,ids.[Name]
      ,[ModifiedDate]
  FROM [AdventureWorks2008R2].[Person].[ContactType] ids
--join to template table
JOIN AdventureWorks2008R2.dbo.TemplateKeyComparatorContactType tcc 
	ON tcc.[Name] = ids.[Name] COLLATE Polish_CI_AS