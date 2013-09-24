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
	UnspecificValue* asUnspecificValue()
	{
		if (typeId() == BoxedTypeId::Unspecific)
		{
			return reinterpret_cast<UnspecificValue*>(this);
		}

		return nullptr;
	}

	const UnspecificValue* asUnspecificValue() const
	{
		if (typeId() == BoxedTypeId::Unspecific)
		{
			return reinterpret_cast<const UnspecificValue*>(this);
		}

		return nullptr;
	}

	bool isUnspecificValue() const
	{
		return typeId() == BoxedTypeId::Unspecific;
	}

	PairValue* asPairValue()
	{
		if (typeId() == BoxedTypeId::Pair)
		{
			return reinterpret_cast<PairValue*>(this);
		}

		return nullptr;
	}

	const PairValue* asPairValue() const
	{
		if (typeId() == BoxedTypeId::Pair)
		{
			return reinterpret_cast<const PairValue*>(this);
		}

		return nullptr;
	}

	bool isPairValue() const
	{
		return typeId() == BoxedTypeId::Pair;
	}

	EmptyListValue* asEmptyListValue()
	{
		if (typeId() == BoxedTypeId::EmptyList)
		{
			return reinterpret_cast<EmptyListValue*>(this);
		}

		return nullptr;
	}

	const EmptyListValue* asEmptyListValue() const
	{
		if (typeId() == BoxedTypeId::EmptyList)
		{
			return reinterpret_cast<const EmptyListValue*>(this);
		}

		return nullptr;
	}

	bool isEmptyListValue() const
	{
		return typeId() == BoxedTypeId::EmptyList;
	}

	StringLikeValue* asStringLikeValue()
	{
		if ((typeId() == BoxedTypeId::String) || (typeId() == BoxedTypeId::Symbol))
		{
			return reinterpret_cast<StringLikeValue*>(this);
		}

		return nullptr;
	}

	const StringLikeValue* asStringLikeValue() const
	{
		if ((typeId() == BoxedTypeId::String) || (typeId() == BoxedTypeId::Symbol))
		{
			return reinterpret_cast<const StringLikeValue*>(this);
		}

		return nullptr;
	}

	bool isStringLikeValue() const
	{
		return (typeId() == BoxedTypeId::String) || (typeId() == BoxedTypeId::Symbol);
	}

	StringValue* asStringValue()
	{
		if (typeId() == BoxedTypeId::String)
		{
			return reinterpret_cast<StringValue*>(this);
		}

		return nullptr;
	}

	const StringValue* asStringValue() const
	{
		if (typeId() == BoxedTypeId::String)
		{
			return reinterpret_cast<const StringValue*>(this);
		}

		return nullptr;
	}

	bool isStringValue() const
	{
		return typeId() == BoxedTypeId::String;
	}

	SymbolValue* asSymbolValue()
	{
		if (typeId() == BoxedTypeId::Symbol)
		{
			return reinterpret_cast<SymbolValue*>(this);
		}

		return nullptr;
	}

	const SymbolValue* asSymbolValue() const
	{
		if (typeId() == BoxedTypeId::Symbol)
		{
			return reinterpret_cast<const SymbolValue*>(this);
		}

		return nullptr;
	}

	bool isSymbolValue() const
	{
		return typeId() == BoxedTypeId::Symbol;
	}

	BooleanValue* asBooleanValue()
	{
		if (typeId() == BoxedTypeId::Boolean)
		{
			return reinterpret_cast<BooleanValue*>(this);
		}

		return nullptr;
	}

	const BooleanValue* asBooleanValue() const
	{
		if (typeId() == BoxedTypeId::Boolean)
		{
			return reinterpret_cast<const BooleanValue*>(this);
		}

		return nullptr;
	}

	bool isBooleanValue() const
	{
		return typeId() == BoxedTypeId::Boolean;
	}

	ExactIntegerValue* asExactIntegerValue()
	{
		if (typeId() == BoxedTypeId::ExactInteger)
		{
			return reinterpret_cast<ExactIntegerValue*>(this);
		}

		return nullptr;
	}

	const ExactIntegerValue* asExactIntegerValue() const
	{
		if (typeId() == BoxedTypeId::ExactInteger)
		{
			return reinterpret_cast<const ExactIntegerValue*>(this);
		}

		return nullptr;
	}

	bool isExactIntegerValue() const
	{
		return typeId() == BoxedTypeId::ExactInteger;
	}

	InexactRationalValue* asInexactRationalValue()
	{
		if (typeId() == BoxedTypeId::InexactRational)
		{
			return reinterpret_cast<InexactRationalValue*>(this);
		}

		return nullptr;
	}

	const InexactRationalValue* asInexactRationalValue() const
	{
		if (typeId() == BoxedTypeId::InexactRational)
		{
			return reinterpret_cast<const InexactRationalValue*>(this);
		}

		return nullptr;
	}

	bool isInexactRationalValue() const
	{
		return typeId() == BoxedTypeId::InexactRational;
	}

	CharacterValue* asCharacterValue()
	{
		if (typeId() == BoxedTypeId::Character)
		{
			return reinterpret_cast<CharacterValue*>(this);
		}

		return nullptr;
	}

	const CharacterValue* asCharacterValue() const
	{
		if (typeId() == BoxedTypeId::Character)
		{
			return reinterpret_cast<const CharacterValue*>(this);
		}

		return nullptr;
	}

	bool isCharacterValue() const
	{
		return typeId() == BoxedTypeId::Character;
	}

	ByteVectorValue* asByteVectorValue()
	{
		if (typeId() == BoxedTypeId::ByteVector)
		{
			return reinterpret_cast<ByteVectorValue*>(this);
		}

		return nullptr;
	}

	const ByteVectorValue* asByteVectorValue() const
	{
		if (typeId() == BoxedTypeId::ByteVector)
		{
			return reinterpret_cast<const ByteVectorValue*>(this);
		}

		return nullptr;
	}

	bool isByteVectorValue() const
	{
		return typeId() == BoxedTypeId::ByteVector;
	}

	ProcedureValue* asProcedureValue()
	{
		if (typeId() == BoxedTypeId::Procedure)
		{
			return reinterpret_cast<ProcedureValue*>(this);
		}

		return nullptr;
	}

	const ProcedureValue* asProcedureValue() const
	{
		if (typeId() == BoxedTypeId::Procedure)
		{
			return reinterpret_cast<const ProcedureValue*>(this);
		}

		return nullptr;
	}

	bool isProcedureValue() const
	{
		return typeId() == BoxedTypeId::Procedure;
	}

	VectorLikeValue* asVectorLikeValue()
	{
		if (static_cast<int>(typeId()) & 32768)
		{
			return reinterpret_cast<VectorLikeValue*>(this);
		}

		return nullptr;
	}

	const VectorLikeValue* asVectorLikeValue() const
	{
		if (static_cast<int>(typeId()) & 32768)
		{
			return reinterpret_cast<const VectorLikeValue*>(this);
		}

		return nullptr;
	}

	bool isVectorLikeValue() const
	{
		return static_cast<int>(typeId()) & 32768;
	}

	VectorValue* asVectorValue()
	{
		if (typeId() == BoxedTypeId::Vector)
		{
			return reinterpret_cast<VectorValue*>(this);
		}

		return nullptr;
	}

	const VectorValue* asVectorValue() const
	{
		if (typeId() == BoxedTypeId::Vector)
		{
			return reinterpret_cast<const VectorValue*>(this);
		}

		return nullptr;
	}

	bool isVectorValue() const
	{
		return typeId() == BoxedTypeId::Vector;
	}

	ClosureValue* asClosureValue()
	{
		if (typeId() == BoxedTypeId::Closure)
		{
			return reinterpret_cast<ClosureValue*>(this);
		}

		return nullptr;
	}

	const ClosureValue* asClosureValue() const
	{
		if (typeId() == BoxedTypeId::Closure)
		{
			return reinterpret_cast<const ClosureValue*>(this);
		}

		return nullptr;
	}

	bool isClosureValue() const
	{
		return typeId() == BoxedTypeId::Closure;
	}

private:
	BoxedTypeId m_typeId;
	GarbageState m_gcState;
