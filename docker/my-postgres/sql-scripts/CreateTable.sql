CREATE TABLE public.personaddress
(
    addressid integer NOT NULL,
    addressline1 character varying(60),
    addressline2 character varying(60),
    city character varying(30),
    stateprovinceid integer,
    postalcode character varying(15),
    modifieddate character varying(30),
    CONSTRAINT firstkey PRIMARY KEY (addressid)
);

ALTER TABLE public.personaddress
    OWNER to dbquality;


CREATE TABLE public.salesorderdetail
(
    "SalesOrderID" integer NOT NULL,
    "SalesOrderDetailID" integer NOT NULL,
    "OrderQty" integer NOT NULL,
    "ProductID" integer NOT NULL,
    "SpecialOfferID" integer NOT NULL,
    "UnitPrice" numeric(18, 6) NOT NULL,
    "UnitPriceDiscount" numeric(18, 6) NOT NULL,
    "LineTotal" numeric(18, 6) NOT NULL
);


ALTER TABLE public.salesorderdetail
    OWNER to dbquality;
	
	

CREATE TABLE public.salesorderdetail_copy
(
    "SalesOrderID" integer NOT NULL,
    "SalesOrderDetailID" integer NOT NULL,
    "OrderQty" integer NOT NULL,
    "ProductID" integer NOT NULL,
    "SpecialOfferID" integer NOT NULL,
    "UnitPrice" numeric(18, 6) NOT NULL,
    "UnitPriceDiscount" numeric(18, 6) NOT NULL,
    "LineTotal" numeric(18, 6) NOT NULL
);


ALTER TABLE public.salesorderdetail_copy
    OWNER to dbquality;