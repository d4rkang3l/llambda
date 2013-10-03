/*****************************************************************
 * This file is generated by gen-types.py. Do not edit manually. *
 *****************************************************************/

public:
	BoxedTypeId typeId() const
	{
		return m_typeId;
	}

	GarbageState gcState() const
	{
		return m_gcState;
	}

public:
	BoxedUnspecific* asBoxedUnspecific()
	{
		if ((typeId() == BoxedTypeId::Unspecific))
		{
			return reinterpret_cast<BoxedUnspecific*>(this);
		}

		return nullptr;
	}

	const BoxedUnspecific* asBoxedUnspecific() const
	{
		if ((typeId() == BoxedTypeId::Unspecific))
		{
			return reinterpret_cast<const BoxedUnspecific*>(this);
		}

		return nullptr;
	}

	bool isBoxedUnspecific() const
	{
		return (typeId() == BoxedTypeId::Unspecific);
	}

	BoxedListElement* asBoxedListElement()
	{
		if ((typeId() == BoxedTypeId::Pair) || (typeId() == BoxedTypeId::EmptyList))
		{
			return reinterpret_cast<BoxedListElement*>(this);
		}

		return nullptr;
	}

	const BoxedListElement* asBoxedListElement() const
	{
		if ((typeId() == BoxedTypeId::Pair) || (typeId() == BoxedTypeId::EmptyList))
		{
			return reinterpret_cast<const BoxedListElement*>(this);
		}

		return nullptr;
	}

	bool isBoxedListElement() const
	{
		return (typeId() == BoxedTypeId::Pair) || (typeId() == BoxedTypeId::EmptyList);
	}

	BoxedPair* asBoxedPair()
	{
		if ((typeId() == BoxedTypeId::Pair))
		{
			return reinterpret_cast<BoxedPair*>(this);
		}

		return nullptr;
	}

	const BoxedPair* asBoxedPair() const
	{
		if ((typeId() == BoxedTypeId::Pair))
		{
			return reinterpret_cast<const BoxedPair*>(this);
		}

		return nullptr;
	}

	bool isBoxedPair() const
	{
		return (typeId() == BoxedTypeId::Pair);
	}

	BoxedEmptyList* asBoxedEmptyList()
	{
		if ((typeId() == BoxedTypeId::EmptyList))
		{
			return reinterpret_cast<BoxedEmptyList*>(this);
		}

		return nullptr;
	}

	const BoxedEmptyList* asBoxedEmptyList() const
	{
		if ((typeId() == BoxedTypeId::EmptyList))
		{
			return reinterpret_cast<const BoxedEmptyList*>(this);
		}

		return nullptr;
	}

	bool isBoxedEmptyList() const
	{
		return (typeId() == BoxedTypeId::EmptyList);
	}

	BoxedStringLike* asBoxedStringLike()
	{
		if ((typeId() == BoxedTypeId::String) || (typeId() == BoxedTypeId::Symbol))
		{
			return reinterpret_cast<BoxedStringLike*>(this);
		}

		return nullptr;
	}

	const BoxedStringLike* asBoxedStringLike() const
	{
		if ((typeId() == BoxedTypeId::String) || (typeId() == BoxedTypeId::Symbol))
		{
			return reinterpret_cast<const BoxedStringLike*>(this);
		}

		return nullptr;
	}

	bool isBoxedStringLike() const
	{
		return (typeId() == BoxedTypeId::String) || (typeId() == BoxedTypeId::Symbol);
	}

	BoxedString* asBoxedString()
	{
		if ((typeId() == BoxedTypeId::String))
		{
			return reinterpret_cast<BoxedString*>(this);
		}

		return nullptr;
	}

	const BoxedString* asBoxedString() const
	{
		if ((typeId() == BoxedTypeId::String))
		{
			return reinterpret_cast<const BoxedString*>(this);
		}

		return nullptr;
	}

	bool isBoxedString() const
	{
		return (typeId() == BoxedTypeId::String);
	}

	BoxedSymbol* asBoxedSymbol()
	{
		if ((typeId() == BoxedTypeId::Symbol))
		{
			return reinterpret_cast<BoxedSymbol*>(this);
		}

		return nullptr;
	}

	const BoxedSymbol* asBoxedSymbol() const
	{
		if ((typeId() == BoxedTypeId::Symbol))
		{
			return reinterpret_cast<const BoxedSymbol*>(this);
		}

		return nullptr;
	}

	bool isBoxedSymbol() const
	{
		return (typeId() == BoxedTypeId::Symbol);
	}

	BoxedBoolean* asBoxedBoolean()
	{
		if ((typeId() == BoxedTypeId::Boolean))
		{
			return reinterpret_cast<BoxedBoolean*>(this);
		}

		return nullptr;
	}

	const BoxedBoolean* asBoxedBoolean() const
	{
		if ((typeId() == BoxedTypeId::Boolean))
		{
			return reinterpret_cast<const BoxedBoolean*>(this);
		}

		return nullptr;
	}

	bool isBoxedBoolean() const
	{
		return (typeId() == BoxedTypeId::Boolean);
	}

	BoxedNumeric* asBoxedNumeric()
	{
		if ((typeId() == BoxedTypeId::ExactInteger) || (typeId() == BoxedTypeId::InexactRational))
		{
			return reinterpret_cast<BoxedNumeric*>(this);
		}

		return nullptr;
	}

	const BoxedNumeric* asBoxedNumeric() const
	{
		if ((typeId() == BoxedTypeId::ExactInteger) || (typeId() == BoxedTypeId::InexactRational))
		{
			return reinterpret_cast<const BoxedNumeric*>(this);
		}

		return nullptr;
	}

	bool isBoxedNumeric() const
	{
		return (typeId() == BoxedTypeId::ExactInteger) || (typeId() == BoxedTypeId::InexactRational);
	}

	BoxedExactInteger* asBoxedExactInteger()
	{
		if ((typeId() == BoxedTypeId::ExactInteger))
		{
			return reinterpret_cast<BoxedExactInteger*>(this);
		}

		return nullptr;
	}

	const BoxedExactInteger* asBoxedExactInteger() const
	{
		if ((typeId() == BoxedTypeId::ExactInteger))
		{
			return reinterpret_cast<const BoxedExactInteger*>(this);
		}

		return nullptr;
	}

	bool isBoxedExactInteger() const
	{
		return (typeId() == BoxedTypeId::ExactInteger);
	}

	BoxedInexactRational* asBoxedInexactRational()
	{
		if ((typeId() == BoxedTypeId::InexactRational))
		{
			return reinterpret_cast<BoxedInexactRational*>(this);
		}

		return nullptr;
	}

	const BoxedInexactRational* asBoxedInexactRational() const
	{
		if ((typeId() == BoxedTypeId::InexactRational))
		{
			return reinterpret_cast<const BoxedInexactRational*>(this);
		}

		return nullptr;
	}

	bool isBoxedInexactRational() const
	{
		return (typeId() == BoxedTypeId::InexactRational);
	}

	BoxedCharacter* asBoxedCharacter()
	{
		if ((typeId() == BoxedTypeId::Character))
		{
			return reinterpret_cast<BoxedCharacter*>(this);
		}

		return nullptr;
	}

	const BoxedCharacter* asBoxedCharacter() const
	{
		if ((typeId() == BoxedTypeId::Character))
		{
			return reinterpret_cast<const BoxedCharacter*>(this);
		}

		return nullptr;
	}

	bool isBoxedCharacter() const
	{
		return (typeId() == BoxedTypeId::Character);
	}

	BoxedByteVector* asBoxedByteVector()
	{
		if ((typeId() == BoxedTypeId::ByteVector))
		{
			return reinterpret_cast<BoxedByteVector*>(this);
		}

		return nullptr;
	}

	const BoxedByteVector* asBoxedByteVector() const
	{
		if ((typeId() == BoxedTypeId::ByteVector))
		{
			return reinterpret_cast<const BoxedByteVector*>(this);
		}

		return nullptr;
	}

	bool isBoxedByteVector() const
	{
		return (typeId() == BoxedTypeId::ByteVector);
	}

	BoxedProcedure* asBoxedProcedure()
	{
		if ((typeId() == BoxedTypeId::Procedure))
		{
			return reinterpret_cast<BoxedProcedure*>(this);
		}

		return nullptr;
	}

	const BoxedProcedure* asBoxedProcedure() const
	{
		if ((typeId() == BoxedTypeId::Procedure))
		{
			return reinterpret_cast<const BoxedProcedure*>(this);
		}

		return nullptr;
	}

	bool isBoxedProcedure() const
	{
		return (typeId() == BoxedTypeId::Procedure);
	}

	BoxedVectorLike* asBoxedVectorLike()
	{
		if ((static_cast<int>(typeId()) & 0x8000))
		{
			return reinterpret_cast<BoxedVectorLike*>(this);
		}

		return nullptr;
	}

	const BoxedVectorLike* asBoxedVectorLike() const
	{
		if ((static_cast<int>(typeId()) & 0x8000))
		{
			return reinterpret_cast<const BoxedVectorLike*>(this);
		}

		return nullptr;
	}

	bool isBoxedVectorLike() const
	{
		return (static_cast<int>(typeId()) & 0x8000);
	}

	BoxedVector* asBoxedVector()
	{
		if ((typeId() == BoxedTypeId::Vector))
		{
			return reinterpret_cast<BoxedVector*>(this);
		}

		return nullptr;
	}

	const BoxedVector* asBoxedVector() const
	{
		if ((typeId() == BoxedTypeId::Vector))
		{
			return reinterpret_cast<const BoxedVector*>(this);
		}

		return nullptr;
	}

	bool isBoxedVector() const
	{
		return (typeId() == BoxedTypeId::Vector);
	}

	BoxedClosure* asBoxedClosure()
	{
		if ((typeId() == BoxedTypeId::Closure))
		{
			return reinterpret_cast<BoxedClosure*>(this);
		}

		return nullptr;
	}

	const BoxedClosure* asBoxedClosure() const
	{
		if ((typeId() == BoxedTypeId::Closure))
		{
			return reinterpret_cast<const BoxedClosure*>(this);
		}

		return nullptr;
	}

	bool isBoxedClosure() const
	{
		return (typeId() == BoxedTypeId::Closure);
	}

private:
	BoxedTypeId m_typeId;
	GarbageState m_gcState;
