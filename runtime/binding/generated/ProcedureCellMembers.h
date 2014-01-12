/************************************************************
 * This file is generated by typegen. Do not edit manually. *
 ************************************************************/

public:
	ProcedureEntryPoint entryPoint() const
	{
		return m_entryPoint;
	}

public:
	static bool isInstance(const DatumCell *datum)
	{
		return datum->typeId() == CellTypeId::Procedure;
	}

	static ProcedureCell* fromDatum(DatumCell *datum)
	{
		if (isInstance(datum))
		{
			return static_cast<ProcedureCell*>(datum);
		}

		return nullptr;
	}

	static const ProcedureCell* fromDatum(const DatumCell *datum)
	{
		if (isInstance(datum))
		{
			return static_cast<const ProcedureCell*>(datum);
		}

		return nullptr;
	}

private:
	ProcedureEntryPoint m_entryPoint;