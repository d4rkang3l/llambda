/*****************************************************************
 * This file is generated by gen-types.py. Do not edit manually. *
 *****************************************************************/

#include "binding/BoxedDatum.h"

using namespace lliby;

extern "C"
{

bool lliby_is_unspecific(const BoxedDatum *value)
{
	return value->isUnspecificValue();
}

bool lliby_is_pair(const BoxedDatum *value)
{
	return value->isPairValue();
}

bool lliby_is_empty_list(const BoxedDatum *value)
{
	return value->isEmptyListValue();
}

bool lliby_is_string_like(const BoxedDatum *value)
{
	return value->isStringLikeValue();
}

bool lliby_is_string(const BoxedDatum *value)
{
	return value->isStringValue();
}

bool lliby_is_symbol(const BoxedDatum *value)
{
	return value->isSymbolValue();
}

bool lliby_is_boolean(const BoxedDatum *value)
{
	return value->isBooleanValue();
}

bool lliby_is_exact_integer(const BoxedDatum *value)
{
	return value->isExactIntegerValue();
}

bool lliby_is_inexact_rational(const BoxedDatum *value)
{
	return value->isInexactRationalValue();
}

bool lliby_is_character(const BoxedDatum *value)
{
	return value->isCharacterValue();
}

bool lliby_is_byte_vector(const BoxedDatum *value)
{
	return value->isByteVectorValue();
}

bool lliby_is_procedure(const BoxedDatum *value)
{
	return value->isProcedureValue();
}

bool lliby_is_vector_like(const BoxedDatum *value)
{
	return value->isVectorLikeValue();
}

bool lliby_is_vector(const BoxedDatum *value)
{
	return value->isVectorValue();
}

bool lliby_is_closure(const BoxedDatum *value)
{
	return value->isClosureValue();
}


}
