/************************************************************
 * This file is generated by typegen. Do not edit manually. *
 ************************************************************/

public:
	std::weak_ptr<actor::Mailbox> mailbox() const
	{
		return m_mailbox;
	}

public:
	static bool typeIdIsTypeOrSubtype(CellTypeId typeId)
	{
		return typeId == CellTypeId::Mailbox;
	}

	static bool isInstance(const AnyCell *cell)
	{
		return typeIdIsTypeOrSubtype(cell->typeId());
	}

private:
	std::weak_ptr<actor::Mailbox> m_mailbox;
