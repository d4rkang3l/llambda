#include "util/utf8ExceptionToSchemeError.h"
#include "util/portCellToStream.h"

#include "unicode/utf8/InvalidByteSequenceException.h"

#include "reader/DatumReader.h"
#include "reader/ReadErrorException.h"

#include "core/error.h"

using namespace lliby;

extern "C"
{

AnyCell *llread_read(World &world, PortCell *portCell)
{
	std::istream *portStream = portCellToInputStream(world, portCell);

	try
	{
		DatumReader reader(world, *portStream);
		return reader.parse();
	}
	catch(const ReadErrorException &e)
	{
		signalError(world, ErrorCategory::Read, e.message());
	}
	catch(const utf8::InvalidByteSequenceException &e)
	{
		utf8ExceptionToSchemeError(world, "(read)", e);
	}
}

}
