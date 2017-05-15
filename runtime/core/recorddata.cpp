#include <cstdlib>
#include <cstdint>

#include "binding/RecordLikeCell.h"

extern "C"
{

using namespace lliby;

void *llcore_record_data_alloc(std::uint32_t size)
{
	return RecordLikeCell::allocateRecordData(size);
}

}
