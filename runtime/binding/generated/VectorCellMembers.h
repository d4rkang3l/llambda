/************************************************************
 * This file is generated by typegen. Do not edit manually. *
 ************************************************************/

public:
	std::uint32_t length() const
	{
		return m_length;
	}

	DatumCell** elements() const
	{
		return m_elements;
	}

public:
	static bool isInstance(const DatumCell *datum)
	{
		return datum->typeId() == CellTypeId::Vector;
	}

	static VectorCell* fromDatum(DatumCell *datum)
	{
		if (isInstance(datum))
		{
			return static_cast<VectorCell*>(datum);
		}

		return nullptr;
	}

	static const VectorCell* fromDatum(const DatumCell *datum)
	{
		if (isInstance(datum))
		{
			return static_cast<const VectorCell*>(datum);
		}

		return nullptr;
	}

private:
	std::uint32_t m_length;
	DatumCell** m_elements;